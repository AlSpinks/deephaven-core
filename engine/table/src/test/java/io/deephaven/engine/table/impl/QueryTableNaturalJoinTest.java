//
// Copyright (c) 2016-2025 Deephaven Data Labs and Patent Pending
//
package io.deephaven.engine.table.impl;

import io.deephaven.api.NaturalJoinType;
import io.deephaven.base.FileUtils;
import io.deephaven.chunk.ObjectChunk;
import io.deephaven.engine.context.ExecutionContext;
import io.deephaven.engine.primitive.iterator.CloseablePrimitiveIteratorOfLong;
import io.deephaven.engine.rowset.*;
import io.deephaven.engine.table.*;
import io.deephaven.engine.table.impl.indexer.DataIndexer;
import io.deephaven.engine.table.impl.select.MatchPairFactory;
import io.deephaven.engine.table.impl.util.ColumnHolder;
import io.deephaven.engine.table.impl.util.RuntimeMemory;
import io.deephaven.engine.table.vectors.ColumnVectors;
import io.deephaven.engine.testutil.*;
import io.deephaven.engine.testutil.generator.*;
import io.deephaven.engine.testutil.testcase.RefreshingTableTestCase;
import io.deephaven.engine.util.TableTools;
import io.deephaven.parquet.table.ParquetInstructions;
import io.deephaven.parquet.table.ParquetTools;
import io.deephaven.test.types.OutOfBandTest;
import io.deephaven.time.DateTimeUtils;
import io.deephaven.util.QueryConstants;
import io.deephaven.util.mutable.MutableInt;
import io.deephaven.util.type.ArrayTypeUtils;
import io.deephaven.vector.IntVector;
import io.deephaven.vector.ObjectVector;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import static io.deephaven.engine.testutil.GenerateTableUpdates.generateAppends;
import static io.deephaven.engine.testutil.TstUtils.*;
import static io.deephaven.engine.util.TableTools.*;
import static io.deephaven.util.QueryConstants.NULL_INT;
import static org.junit.Assert.assertArrayEquals;

@Category(OutOfBandTest.class)
public class QueryTableNaturalJoinTest extends QueryTableTestBase {

    public void testNaturalJoinRehash() {
        setExpectError(false);

        final Random random = new Random(0);

        final String[] leftJoinKey = new String[1024];
        final int[] leftSentinel = new int[1024];
        final String[] rightJoinKey = new String[1024];
        final int[] rightSentinel = new int[1024];

        int offset = 0;
        fillRehashKeys(offset, leftJoinKey, leftSentinel, rightJoinKey, rightSentinel);

        final QueryTable leftTable =
                TstUtils.testRefreshingTable(stringCol("JoinKey", leftJoinKey), intCol("LeftSentinel", leftSentinel));
        final QueryTable rightTable = TstUtils.testRefreshingTable(stringCol("JoinKey", rightJoinKey),
                intCol("RightSentinel", rightSentinel));

        final EvalNugget[] en = new EvalNugget[] {
                new EvalNugget() {
                    public Table e() {
                        return leftTable.naturalJoin(rightTable, "JoinKey", "RJK=JoinKey,RightSentinel");
                    }
                },
        };

        if (printTableUpdates) {
            for (int ii = 0; ii < en.length; ++ii) {
                en[ii].showResult("Original " + ii, en[ii].originalValue);
            }
        }

        for (int step = 0; step < 40; step++) {
            System.out
                    .println("Step = " + step + ", leftSize=" + leftTable.size() + ", rightSize=" + rightTable.size());

            offset += leftJoinKey.length;
            fillRehashKeys(offset, leftJoinKey, leftSentinel, rightJoinKey, rightSentinel);

            final int foffset = offset;
            // make something that exists go away
            // make something that did not exist come back
            final ControlledUpdateGraph updateGraph = ExecutionContext.getContext().getUpdateGraph().cast();
            updateGraph.runWithinUnitTestCycle(() -> {
                final RowSet addRowSet = RowSetFactory.fromRange(foffset, foffset + leftJoinKey.length - 1);
                addToTable(leftTable, addRowSet, stringCol("JoinKey", leftJoinKey),
                        intCol("LeftSentinel", leftSentinel));
                leftTable.notifyListeners(addRowSet.copy(), i(), i());


                final RowSetBuilderSequential modIndexBuilder = RowSetFactory.builderSequential();

                int slot = random.nextInt(foffset / 100);
                for (int ii = 0; ii < 100; ++ii) {
                    modIndexBuilder.appendKey(slot);
                    slot += 1 + random.nextInt(foffset / 100);
                    if (slot >= foffset) {
                        break;
                    }
                }

                final RowSet modRowSet = modIndexBuilder.build();
                final String[] rightModifications = new String[modRowSet.intSize()];
                final int[] rightModifySentinel = new int[modRowSet.intSize()];

                final MutableInt position = new MutableInt();
                modRowSet.forAllRowKeys((long ll) -> {
                    final int ii = (int) ll;
                    if (ii % 2 == 0) {
                        // make something that exists go away
                        rightModifications[position.get()] = Integer.toString(ii * 10 + 2);
                    } else {
                        // make something that did not exist come back
                        rightModifications[position.get()] = Integer.toString(ii * 10);
                    }
                    rightModifySentinel[position.get()] = ii * 100 + 25;
                    position.increment();
                });

                addToTable(rightTable, addRowSet, stringCol("JoinKey", rightJoinKey),
                        intCol("RightSentinel", rightSentinel));
                addToTable(rightTable, modRowSet, stringCol("JoinKey", rightModifications),
                        intCol("RightSentinel", rightModifySentinel));
                rightTable.notifyListeners(addRowSet, i(), modRowSet);
            });
            TstUtils.validate(en);
        }
    }

    private void fillRehashKeys(int offset, String[] leftJoinKey, int[] leftSentinel, String[] rightJoinKey,
            int[] rightSentinel) {
        for (int ii = 0; ii < leftJoinKey.length; ii++) {
            final int iio = ii + offset;
            leftJoinKey[ii] = Integer.toString(iio * 10);
            leftSentinel[ii] = iio * 100;

            if (iio % 2 == 0) {
                rightJoinKey[ii] = Integer.toString(iio * 10);
            } else {
                rightJoinKey[ii] = Integer.toString((iio / 4 * 10) + 1);
            }
            rightSentinel[ii] = iio * 100 + 25;
        }
    }

    public void testNaturalJoinIncremental() {
        setExpectError(false);

        final int sz = 5;
        final int maxSteps = 10;
        for (JoinIncrement joinIncrement : joinIncrementorsShift) {
            testNaturalJoinIncremental(false, false, sz, sz, false, false, joinIncrement, 0, maxSteps);
            testNaturalJoinIncremental(false, false, sz, sz, true, false, joinIncrement, 0, maxSteps);
            testNaturalJoinIncremental(false, false, sz, sz, false, true, joinIncrement, 0, maxSteps);
            testNaturalJoinIncremental(false, false, sz, sz, true, true, joinIncrement, 0, maxSteps);
        }

        final int[] leftSizes = new int[] {10, 50, 100};
        final int[] rightSizes = new int[] {10, 50, 100};
        for (int leftSize : leftSizes) {
            for (int rightSize : rightSizes) {
                for (long seed = 0; seed < 5; seed++) {
                    System.out.println("leftSize=" + leftSize + ", rightSize=" + rightSize + ", seed=" + seed);
                    for (JoinIncrement joinIncrement : joinIncrementorsShift) {
                        testNaturalJoinIncremental(false, false, leftSize, rightSize, false, false, joinIncrement, seed,
                                maxSteps);
                        testNaturalJoinIncremental(false, false, leftSize, rightSize, true, false, joinIncrement, seed,
                                maxSteps);
                        testNaturalJoinIncremental(false, false, leftSize, rightSize, false, true, joinIncrement, seed,
                                maxSteps);
                        testNaturalJoinIncremental(false, false, leftSize, rightSize, true, true, joinIncrement, seed,
                                maxSteps);
                    }
                }
            }
        }
    }

    public void testNaturalJoinLeftIncrementalRightStatic() {
        for (JoinIncrement joinIncrement : new JoinIncrement[] {leftStepShift, leftStep}) {
            final int sz = 5;
            final int maxSteps = 20;
            testNaturalJoinIncremental(false, true, sz, sz, false, false, joinIncrement, 0, maxSteps);
            testNaturalJoinIncremental(false, true, sz, sz, true, false, joinIncrement, 0, maxSteps);
            testNaturalJoinIncremental(false, true, sz, sz, false, true, joinIncrement, 0, maxSteps);
            testNaturalJoinIncremental(false, true, sz, sz, true, true, joinIncrement, 0, maxSteps);

            final int[] leftSizes = new int[] {50, 100};
            final int[] rightSizes = new int[] {50, 100};
            for (long seed = 0; seed < 1; seed++) {
                for (int leftSize : leftSizes) {
                    for (int rightSize : rightSizes) {
                        testNaturalJoinIncremental(false, true, leftSize, rightSize, false, false, joinIncrement, seed,
                                maxSteps);
                        testNaturalJoinIncremental(false, true, leftSize, rightSize, true, false, joinIncrement, seed,
                                maxSteps);
                        testNaturalJoinIncremental(false, true, leftSize, rightSize, false, true, joinIncrement, seed,
                                maxSteps);
                        testNaturalJoinIncremental(false, true, leftSize, rightSize, true, true, joinIncrement, seed,
                                maxSteps);
                    }
                }
            }
        }
    }

    public void testNaturalJoinLeftStaticRightIncremental() {
        for (JoinIncrement joinIncrement : new JoinIncrement[] {rightStepShift, rightStep}) {
            final int sz = 5;
            final int maxSteps = 20;
            testNaturalJoinIncremental(true, false, sz, sz, false, false, joinIncrement, 0, maxSteps);
            testNaturalJoinIncremental(true, false, sz, sz, true, false, joinIncrement, 0, maxSteps);
            testNaturalJoinIncremental(true, false, sz, sz, false, true, joinIncrement, 0, maxSteps);
            testNaturalJoinIncremental(true, false, sz, sz, true, true, joinIncrement, 0, maxSteps);

            final int[] leftSizes = new int[] {50, 100};
            final int[] rightSizes = new int[] {50, 100};
            for (long seed = 0; seed < 5; seed++) {
                for (int leftSize : leftSizes) {
                    for (int rightSize : rightSizes) {
                        testNaturalJoinIncremental(true, false, leftSize, rightSize, false, false, joinIncrement, seed,
                                maxSteps);
                        testNaturalJoinIncremental(true, false, leftSize, rightSize, true, false, joinIncrement, seed,
                                maxSteps);
                        testNaturalJoinIncremental(true, false, leftSize, rightSize, false, true, joinIncrement, seed,
                                maxSteps);
                        testNaturalJoinIncremental(true, false, leftSize, rightSize, true, true, joinIncrement, seed,
                                maxSteps);
                    }
                }
            }
        }
    }

    private void testNaturalJoinIncremental(boolean leftStatic, boolean rightStatic, int leftSize, int rightSize,
            boolean leftIndexed, boolean rightIndexed,
            JoinIncrement joinIncrement, long seed, int maxSteps) {
        testNaturalJoinIncremental(leftStatic, rightStatic, leftSize, rightSize, leftIndexed, rightIndexed,
                joinIncrement, seed, new MutableInt(maxSteps));
    }

    private void testNaturalJoinIncremental(boolean leftStatic, boolean rightStatic, int leftSize, int rightSize,
            boolean leftIndexed, boolean rightIndexed,
            JoinIncrement joinIncrement, long seed, MutableInt numSteps) {
        testNaturalJoinIncremental(leftStatic, rightStatic, leftSize, rightSize, leftIndexed, rightIndexed,
                joinIncrement, seed, numSteps, new JoinControl());
    }

    private static void testNaturalJoinIncremental(boolean leftStatic, boolean rightStatic, int leftSize, int rightSize,
            boolean leftIndexed, boolean rightIndexed, JoinIncrement joinIncrement, long seed, int maxSteps,
            JoinControl control) {
        testNaturalJoinIncremental(leftStatic, rightStatic, leftSize, rightSize, leftIndexed, rightIndexed,
                joinIncrement, seed, new MutableInt(maxSteps), control);
    }

    private static void testNaturalJoinIncremental(boolean leftStatic, boolean rightStatic, int leftSize, int rightSize,
            boolean leftIndexed, boolean rightIndexed, JoinIncrement joinIncrement, long seed, MutableInt numSteps,
            JoinControl control) {
        final Random random = new Random(seed);
        final int maxSteps = numSteps.get();

        final ColumnInfo<?, ?>[] rightColumnInfo;
        final UniqueIntGenerator rightIntGenerator =
                new UniqueIntGenerator(1, rightSize * (rightStatic ? 2 : 4));
        final UniqueIntGenerator rightInt2Generator =
                new UniqueIntGenerator(1, rightSize * (rightStatic ? 2 : 4));

        final IntGenerator duplicateGenerator = new IntGenerator(100000, 100010);

        final List<TestDataGenerator<Integer, Integer>> generatorList =
                Arrays.asList(rightIntGenerator, duplicateGenerator);
        final TestDataGenerator<Integer, Integer> compositeGenerator =
                new CompositeGenerator<>(generatorList, 0.9);

        final QueryTable rightTable = getTable(!rightStatic, rightSize, random,
                rightColumnInfo = initColumnInfos(new String[] {"I1", "C1", "C2"},
                        compositeGenerator,
                        new SetGenerator<>("a", "b"),
                        rightInt2Generator));
        if (rightIndexed) {
            DataIndexer.getOrCreateDataIndex(rightTable, "I1");
            DataIndexer.getOrCreateDataIndex(rightTable, "I1", "C1");
            DataIndexer.getOrCreateDataIndex(rightTable, "I1", "C1", "C2");
        }

        final ColumnInfo<?, ?>[] leftColumnInfo;
        final QueryTable leftTable = getTable(!leftStatic, leftSize, random,
                leftColumnInfo = initColumnInfos(new String[] {"I1", "C1", "C2"},
                        new FromUniqueIntGenerator(rightIntGenerator, new IntGenerator(20, 10000), 0.75),
                        new SetGenerator<>("a", "b", "c"),
                        new FromUniqueIntGenerator(rightInt2Generator, new IntGenerator(20, 10000), 0.75)));
        if (leftIndexed) {
            DataIndexer.getOrCreateDataIndex(leftTable, "I1");
            DataIndexer.getOrCreateDataIndex(leftTable, "I1", "C1");
            DataIndexer.getOrCreateDataIndex(leftTable, "I1", "C1", "C2");
        }

        final EvalNugget[] en = new EvalNugget[] {
                new EvalNugget() {
                    public Table e() {
                        return NaturalJoinHelper.naturalJoin(leftTable, rightTable,
                                MatchPairFactory.getExpressions("I1"),
                                MatchPairFactory.getExpressions("RI1=I1", "RC1=C1", "RC2=C2"),
                                NaturalJoinType.ERROR_ON_DUPLICATE, control);
                    }
                },
                new EvalNugget() {
                    public Table e() {
                        return NaturalJoinHelper.naturalJoin(leftTable, rightTable,
                                MatchPairFactory.getExpressions("C1", "I1"), MatchPairFactory.getExpressions("RC2=C2"),
                                NaturalJoinType.ERROR_ON_DUPLICATE, control);
                    }
                },
                new EvalNugget() {
                    public Table e() {
                        return NaturalJoinHelper.naturalJoin(leftTable, (QueryTable) rightTable.update("Exists=true"),
                                MatchPairFactory.getExpressions("C1", "C2", "I1"),
                                MatchPairFactory.getExpressions("Exists"), NaturalJoinType.ERROR_ON_DUPLICATE, control);
                    }
                },
        };

        if (printTableUpdates) {
            for (int ii = 0; ii < en.length; ++ii) {
                en[ii].showResult("Original " + ii, en[ii].originalValue);
            }
        }

        final int leftStepSize = (int) Math.ceil(Math.sqrt(leftSize));
        final int rightStepSize = (int) Math.ceil(Math.sqrt(rightSize));

        for (numSteps.set(0); numSteps.get() < maxSteps; numSteps.increment()) {
            if (printTableUpdates) {
                System.out.println("Step = " + numSteps.get() + ", leftSize=" + leftSize + ", rightSize="
                        + rightSize + ", seed = " + seed + ", joinIncrement=" + joinIncrement);
                System.out.println("Left Table:" + leftTable.size());
                TableTools.showWithRowSet(leftTable, 100);
                System.out.println("Right Table:" + rightTable.size());
                TableTools.showWithRowSet(rightTable, 100);
            }
            joinIncrement.step(leftStepSize, rightStepSize, leftTable, rightTable, leftColumnInfo, rightColumnInfo, en,
                    random);
        }
    }

    public void testNaturalJoinMixedGrouping() {
        testNaturalJoinMixedGroupingLeftStatic(10, 10, 0, 20);
        testNaturalJoinMixedGroupingLeftStatic(1000, 1000, 1, 10);
        testNaturalJoinMixedGroupingLeftStatic(10000, 10000, 1, 10);
    }

    private void testNaturalJoinMixedGroupingLeftStatic(int leftSize, int rightSize, long seed, int steps) {
        final Random random = new Random(seed);

        final QueryTable leftTable = getTable(false, leftSize, random, initColumnInfos(new String[] {"I1", "C1", "C2"},
                new ColumnInfo.ColAttributes[] {ColumnInfo.ColAttributes.Indexed},
                new IntGenerator(1, rightSize * 10),
                new SetGenerator<>("a", "b", "c", "d", "e", "f"),
                new IntGenerator(1, 10)));
        final ColumnInfo<?, ?>[] rightColumnInfos = initColumnInfos(new String[] {"I1", "C1", "C2"},
                new ColumnInfo.ColAttributes[] {},
                new UniqueIntGenerator(1, rightSize * 10),
                new SetGenerator<>("a", "b", "c", "d", "e"),
                new IntGenerator(1, 10));
        final QueryTable rightTable = getTable(true, rightSize, random, rightColumnInfos);

        System.out.println("leftSize=" + leftSize + ", rightSize=" + rightSize + ", seed=" + seed);

        if (RefreshingTableTestCase.printTableUpdates) {
            System.out.println("Left:");
            TableTools.showWithRowSet(leftTable);
            System.out.println("Right:");
            TableTools.showWithRowSet(rightTable);
        }

        final Table result = leftTable.naturalJoin(rightTable, "I1", "LC1=C1,LC2=C2");

        if (RefreshingTableTestCase.printTableUpdates) {
            System.out.println("Result:");
            TableTools.showWithRowSet(result);
        }

        final Table noGroupingResult = leftTable.update("I1=I1*10")
                .naturalJoin(rightTable.update("I1=I1*10"), "I1", "LC1=C1,LC2=C2").update("I1=(int)(I1/10)");

        if (RefreshingTableTestCase.printTableUpdates) {
            System.out.println("Ungrouped Result:");
            TableTools.showWithRowSet(noGroupingResult);
        }

        assertTableEquals(noGroupingResult, result);

        final Table leftFlat = leftTable.flatten();

        // Create the data index for this table and column.
        DataIndexer.getOrCreateDataIndex(leftFlat, "I1");

        final Table resultFlat = leftFlat.naturalJoin(rightTable, "I1", "LC1=C1,LC2=C2");
        assertTableEquals(noGroupingResult, resultFlat);

        final ControlledUpdateGraph updateGraph = ExecutionContext.getContext().getUpdateGraph().cast();
        for (int step = 0; step < steps; ++step) {
            if (RefreshingTableTestCase.printTableUpdates) {
                System.out.println("Step = " + step);
            }

            updateGraph.runWithinUnitTestCycle(() -> {
                GenerateTableUpdates.generateShiftAwareTableUpdates(GenerateTableUpdates.DEFAULT_PROFILE, rightSize,
                        random, rightTable, rightColumnInfos);
            });

            if (RefreshingTableTestCase.printTableUpdates) {
                System.out.println("Expected");
                TableTools.showWithRowSet(result);
                System.out.println("Result");
                TableTools.showWithRowSet(result);
            }

            assertTableEquals(noGroupingResult, result);
            assertTableEquals(noGroupingResult, resultFlat);
        }
    }

    public void testNaturalJoinSimpleStatic() {
        // noinspection unchecked
        testNaturalJoinSimpleStatic(TableTools::col);
    }

    public void testNaturalJoinGroupedStatic() {
        testNaturalJoinSimpleStatic(TstUtils::colIndexed);
    }

    private interface MakeLeftColumn {
        @SuppressWarnings("unchecked")
        <T> ColumnHolder<T> make(String name, T... data);
    }

    private void testNaturalJoinSimpleStatic(MakeLeftColumn lC) {
        final Table left = testTable(lC.make("Symbol", "A", "B", "C"), col("LeftSentinel", 1, 2, 3));
        final Table right = newTable(col("Symbol", "A", "B", "D", "E", "F"), col("RightSentinel", 10, 11, 12, 13, 14),
                col("RightObjectSentinel", 10, 11L, "12", "13", "14"));

        final Table cj = left.naturalJoin(right, "Symbol");
        TableTools.showWithRowSet(cj);
        assertEquals(new int[] {10, 11, NULL_INT}, intColumn(cj, "RightSentinel"));
        // the two wheres check for filling null keys
        final Table cjw = cj.where("RightObjectSentinel = null");
        final Table cjw2 =
                left.naturalJoin(SparseSelect.sparseSelect(right), "Symbol").where("RightObjectSentinel = null");
        TableTools.showWithRowSet(cjw);
        TableTools.showWithRowSet(cjw2);

        final Table left2 = newTable(lC.make("Symbol", "A", "B", "C", "A"), col("LeftSentinel", 1, 2, 3, 4));
        final Table right2 = newTable(col("Symbol", "A", "B", "D"), col("RightSentinel", 10, 11, 12));

        final Table cj2 = left2.naturalJoin(right2, "Symbol");
        TableTools.showWithRowSet(cj2);
        assertEquals(new int[] {10, 11, NULL_INT, 10}, intColumn(cj2, "RightSentinel"));

        final int collision = 16384;
        final Table left3 = newTable(lC.make("Int", 10, collision + 10, collision * 2 + 10, collision * 3 + 10),
                col("LeftSentinel", 1, 3, 3, 4));
        final Table right3 =
                newTable(col("Int", 10, collision + 10, collision * 4 + 10), col("RightSentinel", 10, 11, 13));

        TableTools.show(left3);
        TableTools.show(right3);

        final Table cj3 = left3.naturalJoin(right3, "Int");
        TableTools.showWithRowSet(cj3);
        assertEquals(new int[] {10, 11, NULL_INT, NULL_INT}, intColumn(cj3, "RightSentinel"));

        final Table left4 = newTable(
                lC.make("String", "c", "e", "g"),
                col("LeftSentinel", 1, 2, 3));
        final Table right4 = newTable(col("String", "c", "e"), col("RightSentinel", 10, 11));
        final Table cj4 = left4.naturalJoin(right4, "String");
        TableTools.showWithRowSet(cj4);
        assertEquals(new int[] {10, 11, NULL_INT}, intColumn(cj4, "RightSentinel"));


        final Table left5 = newTable(
                lC.make("String", "c", "e", "g"),
                col("LeftSentinel", 1, 2, 3));
        final Table right5 = newTable(col("RightSentinel", 10));
        final Table cj5 = left5.naturalJoin(right5, "");
        TableTools.showWithRowSet(cj5);
        assertEquals(new int[] {10, 10, 10}, intColumn(cj5, "RightSentinel"));

        final Table left6 = newTable(
                lC.make("String", "c", "e", "g"),
                col("LeftSentinel", 1, 2, 3));
        final Table right6 = newTable(intCol("RightSentinel"));
        final Table cj6 = left6.naturalJoin(right6, "");
        TableTools.showWithRowSet(cj6);
        assertEquals(new int[] {NULL_INT, NULL_INT, NULL_INT}, intColumn(cj6, "RightSentinel"));

        final Table left7 = newTable(
                lC.make("String", ArrayTypeUtils.EMPTY_STRING_ARRAY),
                intCol("LeftSentinel"));
        final Table right7 = newTable(intCol("RightSentinel", 10, 11));
        final Table cj7 = left7.naturalJoin(right7, "");
        TableTools.showWithRowSet(cj7);
        assertEquals(0, cj7.size());

        // inactive right hand side state, build using the left
        final Table left8 = newTable(lC.make("Symbol", "A", "B", "C"), col("LeftSentinel", 1, 2, 3));
        final Table right8 = newTable(col("Symbol", "A", "B", "D", "D", "E", "E", "D"),
                col("RightSentinel", 10, 11, 12, 13, 14, 15, 16));
        final Table cj8 = left8.naturalJoin(right8, "Symbol");
        TableTools.showWithRowSet(cj8);
        assertEquals(new int[] {10, 11, NULL_INT}, intColumn(cj8, "RightSentinel"));

        // inactive right hand side state, build using the right
        final Table left9 =
                newTable(lC.make("Symbol", "A", "B", "C", "A", "B", "C"), col("LeftSentinel", 1, 2, 3, 4, 5, 6));
        final Table right9 = newTable(col("Symbol", "A", "D", "D"), col("RightSentinel", 10, 11, 12));
        final Table cj9 = left9.naturalJoin(right9, "Symbol");
        TableTools.showWithRowSet(cj9);
        assertEquals(new int[] {10, NULL_INT, NULL_INT, 10, NULL_INT, NULL_INT}, intColumn(cj9, "RightSentinel"));
    }

    public void testNaturalJoinDuplicateRights() {
        // build from right
        final Table left = testTable(col("Symbol", "A", "B", "C", "D"), col("LeftSentinel", 1, 2, 3, 4));
        final Table right = newTable(col("Symbol", "A", "A"), col("RightSentinel", 10, 11));
        try {
            final Table cj = left.naturalJoin(right, "Symbol");
            TableTools.showWithRowSet(cj);
            fail("Expected exception.");
        } catch (IllegalStateException e) {
            assertEquals(dupMsg + "A", e.getMessage());
        }

        // build from left
        final Table left2 = testTable(col("Symbol", "A", "B"), col("LeftSentinel", 1, 2));
        final Table right2 = newTable(col("Symbol", "A", "A", "B", "C", "D"), col("RightSentinel", 10, 11, 12, 13, 14));
        try {
            final Table cj2 = left2.naturalJoin(right2, "Symbol");
            TableTools.showWithRowSet(cj2);
            fail("Expected exception");
        } catch (IllegalStateException e) {
            assertEquals(dupMsg + "A", e.getMessage());
        }
    }

    public void testNaturalJoinDuplicateReinterpret() {
        testNaturalJoinDuplicateRightReinterpret(true, true);
        testNaturalJoinDuplicateRightReinterpret(true, false);
        testNaturalJoinDuplicateRightReinterpret(false, true);
        testNaturalJoinDuplicateRightReinterpret(false, false);
    }

    private void testNaturalJoinDuplicateRightReinterpret(boolean leftRefreshing, boolean rightRefreshing) {
        // build from right
        final Instant instantA = DateTimeUtils.parseInstant("2022-05-06T09:30:00 NY");
        final Instant instantB = DateTimeUtils.parseInstant("2022-05-06T09:31:00 NY");
        final Instant instantC = DateTimeUtils.parseInstant("2022-05-06T09:32:00 NY");
        final Instant instantD = DateTimeUtils.parseInstant("2022-05-06T09:33:00 NY");
        final QueryTable left = testTable(col("JK1", false, null, true), col("JK2", instantA, instantA, instantA),
                col("LeftSentinel", 1, 2, 3));
        left.setRefreshing(leftRefreshing);
        final QueryTable right =
                testTable(col("JK1", true, true), col("JK2", instantA, instantA), col("RightSentinel", 10, 11));
        right.setRefreshing(rightRefreshing);

        try {
            final Table cj = left.naturalJoin(right, "JK1, JK2");
            TableTools.showWithRowSet(cj);
            fail("Expected exception.");
        } catch (IllegalStateException e) {
            assertEquals(dupMsg + "[true, " + instantA + "]", e.getMessage());
        }

        // build from left
        final Table left2 = testTable(col("DT", instantA, instantB), col("LeftSentinel", 1, 2));
        final Table right2 = newTable(col("DT", instantA, instantA, instantB, instantC, instantD),
                col("RightSentinel", 10, 11, 12, 13, 14));
        try {
            final Table cj2 = left2.naturalJoin(right2, "DT");
            TableTools.showWithRowSet(cj2);
            fail("Expected exception");
        } catch (IllegalStateException e) {
            assertEquals(dupMsg + instantA, e.getMessage());
        }
    }

    private final static String dupMsg = "Natural Join found duplicate right key for ";

    private static Instant makeInstantKey(String a) {
        final Instant instantA = DateTimeUtils.parseInstant("2022-05-06T09:30:00 NY");
        final Instant instantB = DateTimeUtils.parseInstant("2022-05-06T09:31:00 NY");
        switch (a) {
            case "A":
                return instantA;
            case "B":
                return instantB;
            default:
                throw new IllegalStateException();
        }
    }

    private static Table castSymbol(Class<?> clazz, Table table) {
        return table.updateView("Symbol=(" + clazz.getCanonicalName() + ")Symbol");
    }

    public void testNaturalJoinDuplicateRightsRefreshingRight() {
        testNaturalJoinDuplicateRightsRefreshingRight(String.class, Function.identity());
        testNaturalJoinDuplicateRightsRefreshingRight(Instant.class, QueryTableNaturalJoinTest::makeInstantKey);
    }

    private <T> void testNaturalJoinDuplicateRightsRefreshingRight(Class<T> clazz, Function<String, T> makeKey) {
        // initial case
        T a = makeKey.apply("A");
        T b = makeKey.apply("B");
        final Table left = castSymbol(clazz, testTable(col("Symbol", a, b), col("LeftSentinel", 1, 2)));
        final Table right = castSymbol(clazz, testRefreshingTable(col("Symbol", a, a), col("RightSentinel", 10, 11)));

        TableTools.showWithRowSet(right.meta());
        TableTools.showWithRowSet(right);

        try {
            final Table cj = left.naturalJoin(right, "Symbol");
            TableTools.showWithRowSet(cj);
            fail("Expected exception.");
        } catch (IllegalStateException rte) {
            assertEquals(dupMsg + a, rte.getMessage());
        }

        // bad right key added
        final QueryTable right2 = testRefreshingTable(col("Symbol", a), col("RightSentinel", 10));
        final Table cj2 = left.naturalJoin(castSymbol(clazz, right2), "Symbol");
        assertTableEquals(
                castSymbol(clazz,
                        newTable(col("Symbol", a, b), intCol("LeftSentinel", 1, 2),
                                intCol("RightSentinel", 10, NULL_INT))),
                cj2);

        final ErrorListener listener = new ErrorListener(cj2);
        cj2.addUpdateListener(listener);

        try (final ErrorExpectation ignored = new ErrorExpectation()) {
            final ControlledUpdateGraph updateGraph = ExecutionContext.getContext().getUpdateGraph().cast();
            updateGraph.runWithinUnitTestCycle(() -> {
                TstUtils.addToTable(right2, i(3), col("Symbol", a), intCol("RightSentinel", 10));
                right2.notifyListeners(i(3), i(), i());
            });
        }

        assertNotNull(listener.originalException());
        assertEquals(dupMsg + a, listener.originalException().getMessage());
    }

    public void testNaturalJoinDuplicateRightsRefreshingBoth() {
        testNaturalJoinDuplicateRightsRefreshingBoth(String.class, Function.identity());
        testNaturalJoinDuplicateRightsRefreshingBoth(Instant.class, QueryTableNaturalJoinTest::makeInstantKey);
    }

    private <T> void testNaturalJoinDuplicateRightsRefreshingBoth(Class<T> clazz, Function<String, T> makeKey) {
        // build from right
        T a = makeKey.apply("A");
        T b = makeKey.apply("B");
        final Table left = castSymbol(clazz, testRefreshingTable(col("Symbol", a, b), col("LeftSentinel", 1, 2)));
        final Table right = castSymbol(clazz, testRefreshingTable(col("Symbol", a, a), col("RightSentinel", 10, 11)));

        try {
            final Table cj = left.naturalJoin(right, "Symbol");
            TableTools.showWithRowSet(cj);
            fail("Expected exception.");
        } catch (IllegalStateException rte) {
            assertEquals(dupMsg + a, rte.getMessage());
        }

        // bad right key added
        final QueryTable right2 = testRefreshingTable(col("Symbol", a), col("RightSentinel", 10));
        final Table cj2 = left.naturalJoin(castSymbol(clazz, right2), "Symbol");
        assertTableEquals(
                castSymbol(clazz,
                        newTable(col("Symbol", a, b), intCol("LeftSentinel", 1, 2),
                                intCol("RightSentinel", 10, NULL_INT))),
                cj2);

        final ErrorListener listener = new ErrorListener(cj2);
        cj2.addUpdateListener(listener);

        try (final ErrorExpectation ignored = new ErrorExpectation()) {
            final ControlledUpdateGraph updateGraph = ExecutionContext.getContext().getUpdateGraph().cast();
            updateGraph.runWithinUnitTestCycle(() -> {
                TstUtils.addToTable(right2, i(3), col("Symbol", a), intCol("RightSentinel", 10));
                right2.notifyListeners(i(3), i(), i());
            });
        }

        assertNotNull(listener.originalException());
        assertEquals(dupMsg + a, listener.originalException().getMessage());
    }


    public void testNaturalJoinReinterprets() {
        final Table left = testTable(col("JBool", true, false, null, true), col("LeftSentinel", 1, 2, 3, 4));
        final Table right = newTable(col("JBool", true, false, null), col("RightSentinel", 10, 11, 12));
        final Table cj = left.naturalJoin(right, "JBool");
        TableTools.showWithRowSet(cj);
        assertEquals(new int[] {10, 11, 12, 10}, intColumn(cj, "RightSentinel"));

        final Instant time1 = DateTimeUtils.parseInstant("2019-05-10T09:45:00 NY");
        final Instant time2 = DateTimeUtils.parseInstant("2019-05-10T21:45:00 NY");

        final Table left2 = testTable(col("JDate", time1, time2, null, time2), col("LeftSentinel", 1, 2, 3, 4));
        final Table right2 = newTable(col("JDate", time2, time1, null), col("RightSentinel", 10, 11, 12));
        final Table cj2 = left2.naturalJoin(right2, "JDate");
        TableTools.showWithRowSet(cj2);
        assertEquals(new int[] {11, 10, 12, 10}, intColumn(cj2, "RightSentinel"));
    }

    public void testNaturalJoinFloats() {
        final Table left = testTable(floatCol("JF", 1.0f, 2.0f, Float.NaN, 3.0f), col("LeftSentinel", 1, 2, 3, 4));
        final Table right = newTable(floatCol("JF", Float.NaN, 1.0f, 2.0f), col("RightSentinel", 10, 11, 12));
        final Table cj = left.naturalJoin(right, "JF");
        TableTools.showWithRowSet(cj);
        assertEquals(new int[] {11, 12, 10, NULL_INT}, intColumn(cj, "RightSentinel"));

        final Table left2 =
                testTable(doubleCol("JD", 10.0, 20.0, Double.NaN, io.deephaven.util.QueryConstants.NULL_DOUBLE),
                        col("LeftSentinel", 1, 2, 3, 4));
        final Table right2 =
                newTable(doubleCol("JD", QueryConstants.NULL_DOUBLE, Double.NaN, 10.0),
                        col("RightSentinel", 10, 11, 12));
        final Table cj2 = left2.naturalJoin(right2, "JD");
        TableTools.showWithRowSet(cj2);
        assertEquals(new int[] {12, NULL_INT, 11, 10}, intColumn(cj2, "RightSentinel"));
    }

    public void testNaturalJoinZeroKeys() {
        setExpectError(false);

        final QueryTable c0 = TstUtils.testRefreshingTable(intCol("Left", 1, 2, 3));
        final QueryTable c1 = TstUtils.testRefreshingTable(intCol("Right"));

        final Table cj = c0.naturalJoin(c1, "");

        final Table emptyRightResult =
                newTable(intCol("Left", 1, 2, 3), intCol("Right", NULL_INT, NULL_INT, NULL_INT));
        assertTableEquals(emptyRightResult, cj);

        TableTools.showWithRowSet(cj);

        final ControlledUpdateGraph updateGraph = ExecutionContext.getContext().getUpdateGraph().cast();
        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(c1, i(1), intCol("Right", 4));
            c1.notifyListeners(i(1), i(), i());
        });

        TableTools.showWithRowSet(cj);

        final Table fourRightResult = newTable(intCol("Left", 1, 2, 3), intCol("Right", 4, 4, 4));
        assertTableEquals(fourRightResult, cj);

        updateGraph.runWithinUnitTestCycle(() -> {
            removeRows(c1, i(1));
            c1.notifyListeners(i(), i(1), i());
        });

        TableTools.showWithRowSet(cj);

        assertTableEquals(emptyRightResult, cj);

        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(c0, i(6), intCol("Left", 6));
            addToTable(c1, i(2), intCol("Right", 5));
            c0.notifyListeners(i(6), i(), i());
            c1.notifyListeners(i(2), i(), i());
        });

        TableTools.showWithRowSet(cj);

        final Table fiveResult = newTable(intCol("Left", 1, 2, 3, 6), intCol("Right", 5, 5, 5, 5));
        assertTableEquals(fiveResult, cj);

    }

    public void testNaturalJoinZeroKeysStaticRight() {
        setExpectError(false);

        final QueryTable c0 = TstUtils.testRefreshingTable(intCol("Left", 1, 2, 3));
        final Table c1 = newTable(intCol("Right"));
        final Table c2 = newTable(intCol("Right", 4));

        final Table cj1 = c0.naturalJoin(c1, "");
        assertTableEquals(newTable(intCol("Left", 1, 2, 3), intCol("Right", NULL_INT, NULL_INT, NULL_INT)), cj1);
        final ControlledUpdateGraph updateGraph = ExecutionContext.getContext().getUpdateGraph().cast();
        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(c0, i(6), intCol("Left", 6));
            c0.notifyListeners(i(6), i(), i());
        });

        TableTools.showWithRowSet(cj1);
        assertTableEquals(newTable(intCol("Left", 1, 2, 3, 6), intCol("Right", NULL_INT, NULL_INT, NULL_INT, NULL_INT)),
                cj1);

        final Table cj2 = c0.naturalJoin(c2, "");
        assertTableEquals(newTable(intCol("Left", 1, 2, 3, 6), intCol("Right", 4, 4, 4, 4)), cj2);
        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(c0, i(7), intCol("Left", 7));
            c0.notifyListeners(i(7), i(), i());
        });

        TableTools.showWithRowSet(cj1);
        assertTableEquals(newTable(intCol("Left", 1, 2, 3, 6, 7), intCol("Right", 4, 4, 4, 4, 4)), cj2);

    }

    public void testNaturalJoinZeroKeysStaticLeft() {
        setExpectError(false);

        final Table c0 = newTable(intCol("Left", 1, 2, 3));
        final QueryTable c1 = TstUtils.testRefreshingTable(intCol("Right"));

        final Table cj = c0.naturalJoin(c1, "");

        final Table emptyRightResult =
                newTable(intCol("Left", 1, 2, 3), intCol("Right", NULL_INT, NULL_INT, NULL_INT));
        assertTableEquals(emptyRightResult, cj);

        TableTools.showWithRowSet(cj);

        final ControlledUpdateGraph updateGraph = ExecutionContext.getContext().getUpdateGraph().cast();
        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(c1, i(1), intCol("Right", 4));
            c1.notifyListeners(i(1), i(), i());
        });

        TableTools.showWithRowSet(cj);

        final Table fourRightResult = newTable(intCol("Left", 1, 2, 3), intCol("Right", 4, 4, 4));
        assertTableEquals(fourRightResult, cj);

        updateGraph.runWithinUnitTestCycle(() -> {
            removeRows(c1, i(1));
            c1.notifyListeners(i(), i(1), i());
        });

        TableTools.showWithRowSet(cj);

        assertTableEquals(emptyRightResult, cj);

        TableTools.showWithRowSet(cj);

        assertTableEquals(emptyRightResult, cj);

    }

    public void testNaturalJoin() {
        final Table c0 = TstUtils.testRefreshingTable(col("USym0", "A", "B"), intCol("X", 1, 2));
        final Table c1 = TstUtils.testRefreshingTable(col("USym1", "A", "D"), intCol("Y", 1, 2));

        Table cj = c0.naturalJoin(c1, "USym0=USym1", "Y");
        cj.select();

        cj = c0.naturalJoin(c1, "USym0=USym1", "USym1,Y");
        cj.select();


        final Table lTable = TstUtils.testRefreshingTable(
                col("String", "a", "b", "c"),
                intCol("Int", 1, 2, 3));
        final Table rTable = TstUtils.testRefreshingTable(
                col("String", "a", "b", "c"),
                intCol("Int", 10, 20, 30));
        final Table result = lTable.naturalJoin(rTable, "String", "Int2=Int");
        assertEquals(3, result.size());
        assertEquals(3, result.numColumns());
        assertEquals("String", result.getDefinition().getColumns().get(0).getName());
        assertEquals("Int", result.getDefinition().getColumns().get(1).getName());
        assertEquals("Int2", result.getDefinition().getColumns().get(2).getName());
        assertArrayEquals(new String[] {"a", "b", "c"},
                ColumnVectors.ofObject(result, "String", String.class).toArray());
        assertArrayEquals(new int[] {1, 2, 3}, ColumnVectors.ofInt(result, "Int").toArray());
        assertArrayEquals(new int[] {10, 20, 30}, ColumnVectors.ofInt(result, "Int2").toArray());


        Table table1 = TstUtils.testRefreshingTable(
                col("String", "c", "e", "g"));

        Table table2 = TstUtils.testRefreshingTable(col("String", "c", "e"), col("v", 1, 2));
        Table pairMatch = table1.naturalJoin(table2, "String", "v");
        assertEquals(3, pairMatch.size());
        assertEquals(2, pairMatch.numColumns());
        assertEquals("String", pairMatch.getDefinition().getColumns().get(0).getName());
        assertEquals("v", pairMatch.getDefinition().getColumns().get(1).getName());
        assertEquals(String.class, pairMatch.getDefinition().getColumns().get(0).getDataType());
        assertEquals(int.class, pairMatch.getDefinition().getColumns().get(1).getDataType());
        assertArrayEquals(new String[] {"c", "e", "g"}, ColumnVectors
                .ofObject(pairMatch, pairMatch.getDefinition().getColumns().get(0).getName(), String.class).toArray());
        assertArrayEquals(new int[] {1, 2, NULL_INT}, ColumnVectors.ofInt(pairMatch, "v").toArray());


        table2 = TstUtils.testRefreshingTable(
                col("String", "c", "e", "g"), col("v", 1, 2, 3));

        pairMatch = table1.naturalJoin(table2, "String", "v");
        assertEquals(3, pairMatch.size());
        assertEquals(2, pairMatch.numColumns());
        assertEquals("String", pairMatch.getDefinition().getColumns().get(0).getName());
        assertEquals("v", pairMatch.getDefinition().getColumns().get(1).getName());
        assertEquals(String.class, pairMatch.getDefinition().getColumns().get(0).getDataType());
        assertEquals(int.class, pairMatch.getDefinition().getColumns().get(1).getDataType());
        assertArrayEquals(new String[] {"c", "e", "g"}, ColumnVectors
                .ofObject(pairMatch, pairMatch.getDefinition().getColumns().get(0).getName(), String.class).toArray());
        assertArrayEquals(new int[] {1, 2, 3}, ColumnVectors.ofInt(pairMatch, "v").toArray());

        pairMatch = table2.naturalJoin(table1, "String", "");
        assertEquals(3, pairMatch.size());
        assertEquals(2, pairMatch.numColumns());
        assertEquals("String", pairMatch.getDefinition().getColumns().get(0).getName());
        assertEquals("v", pairMatch.getDefinition().getColumns().get(1).getName());
        assertEquals(String.class, pairMatch.getDefinition().getColumns().get(0).getDataType());
        assertEquals(int.class, pairMatch.getDefinition().getColumns().get(1).getDataType());
        assertArrayEquals(new String[] {"c", "e", "g"}, ColumnVectors
                .ofObject(pairMatch, pairMatch.getDefinition().getColumns().get(0).getName(), String.class).toArray());
        assertArrayEquals(new int[] {1, 2, 3}, ColumnVectors.ofInt(pairMatch, "v").toArray());

        pairMatch = table1.naturalJoin(table2, "String=String", "v");
        assertEquals(3, pairMatch.size());
        assertEquals(2, pairMatch.numColumns());
        assertEquals("String", pairMatch.getDefinition().getColumns().get(0).getName());
        assertEquals("v", pairMatch.getDefinition().getColumns().get(1).getName());
        assertEquals(String.class, pairMatch.getDefinition().getColumns().get(0).getDataType());
        assertEquals(int.class, pairMatch.getDefinition().getColumns().get(1).getDataType());
        assertArrayEquals(new String[] {"c", "e", "g"}, ColumnVectors
                .ofObject(pairMatch, pairMatch.getDefinition().getColumns().get(0).getName(), String.class).toArray());
        assertArrayEquals(new int[] {1, 2, 3}, ColumnVectors.ofInt(pairMatch, "v").toArray());

        pairMatch = table2.naturalJoin(table1, "String=String", "");

        assertEquals(3, pairMatch.size());
        assertEquals(2, pairMatch.numColumns());
        assertEquals("String", pairMatch.getDefinition().getColumns().get(0).getName());
        assertEquals("v", pairMatch.getDefinition().getColumns().get(1).getName());
        assertEquals(String.class, pairMatch.getDefinition().getColumns().get(0).getDataType());
        assertEquals(int.class, pairMatch.getDefinition().getColumns().get(1).getDataType());
        assertArrayEquals(new String[] {"c", "e", "g"}, ColumnVectors
                .ofObject(pairMatch, pairMatch.getDefinition().getColumns().get(0).getName(), String.class).toArray());
        IntVector vValues = ColumnVectors.ofInt(pairMatch, "v");
        assertEquals(1, vValues.get(0));
        assertEquals(2, vValues.get(1));
        assertEquals(3, vValues.get(2));


        table1 = TstUtils.testRefreshingTable(
                col("String1", "c", "e", "g"));

        table2 = TstUtils.testRefreshingTable(
                col("String2", "c", "e", "g"), col("v", 1, 2, 3));


        pairMatch = table1.naturalJoin(table2, "String1=String2", "String2,v");

        assertEquals(3, pairMatch.size());
        assertEquals(3, pairMatch.numColumns());
        assertEquals("String1", pairMatch.getDefinition().getColumns().get(0).getName());
        assertEquals("String2", pairMatch.getDefinition().getColumns().get(1).getName());
        assertEquals("v", pairMatch.getDefinition().getColumns().get(2).getName());
        assertEquals(String.class, pairMatch.getDefinition().getColumns().get(0).getDataType());
        assertEquals(String.class, pairMatch.getDefinition().getColumns().get(1).getDataType());
        assertEquals(int.class, pairMatch.getDefinition().getColumns().get(2).getDataType());
        assertArrayEquals(new String[] {"c", "e", "g"}, ColumnVectors
                .ofObject(pairMatch, pairMatch.getDefinition().getColumns().get(0).getName(), String.class).toArray());
        assertArrayEquals(new String[] {"c", "e", "g"}, ColumnVectors
                .ofObject(pairMatch, pairMatch.getDefinition().getColumns().get(1).getName(), String.class).toArray());
        assertArrayEquals(new int[] {1, 2, 3},
                ColumnVectors.ofInt(pairMatch, pairMatch.getDefinition().getColumns().get(2).getName()).toArray());


        pairMatch = table2.naturalJoin(table1, "String2=String1", "String1");

        assertEquals(3, pairMatch.size());
        assertEquals(3, pairMatch.numColumns());
        assertEquals("String2", pairMatch.getDefinition().getColumns().get(0).getName());
        assertEquals("v", pairMatch.getDefinition().getColumns().get(1).getName());
        assertEquals("String1", pairMatch.getDefinition().getColumns().get(2).getName());
        assertEquals(String.class, pairMatch.getDefinition().getColumn("String1").getDataType());
        assertEquals(String.class, pairMatch.getDefinition().getColumn("String2").getDataType());
        assertEquals(int.class, pairMatch.getDefinition().getColumn("v").getDataType());
        assertArrayEquals(new String[] {"c", "e", "g"},
                ColumnVectors.ofObject(pairMatch, "String1", String.class).toArray());
        assertArrayEquals(new String[] {"c", "e", "g"},
                ColumnVectors.ofObject(pairMatch, "String2", String.class).toArray());
        assertArrayEquals(new int[] {1, 2, 3}, ColumnVectors.ofInt(pairMatch, "v").toArray());
    }

    public void testNaturalJoinNull() {
        final Table c0 = TstUtils.testRefreshingTable(col("USym0", "A", null), col("X", 1, 2));
        final Table c1 = TstUtils.testRefreshingTable(col("USym1", "A", null), col("Y", 3, 4));

        final Table cj = c0.naturalJoin(c1, "USym0=USym1", "Y");

        TableTools.show(cj);

        final IntVector xValues = ColumnVectors.ofInt(cj, "X");
        assertEquals(1, xValues.get(0));
        assertEquals(2, xValues.get(1));
        final IntVector yValues = ColumnVectors.ofInt(cj, "Y");
        assertEquals(3, yValues.get(0));
        assertEquals(4, yValues.get(1));
    }

    public void testNaturalJoinInactive() {
        setExpectError(false);

        final QueryTable c0 = TstUtils.testRefreshingTable(col("USym0", "A", "C"), col("X", 1, 2));
        final QueryTable c1 = TstUtils.testRefreshingTable(col("USym1", "A", "B", "B"), col("Y", 3, 4, 5));

        final Table cj = c0.naturalJoin(c1, "USym0=USym1", "Y");

        System.out.println("Result:");
        TableTools.showWithRowSet(cj);

        IntVector xValues = ColumnVectors.ofInt(cj, "X");
        assertEquals(1, xValues.get(0));
        assertEquals(2, xValues.get(1));
        IntVector yValues = ColumnVectors.ofInt(cj, "Y");
        assertEquals(3, yValues.get(0));
        assertEquals(NULL_INT, yValues.get(1));

        final ControlledUpdateGraph updateGraph = ExecutionContext.getContext().getUpdateGraph().cast();
        updateGraph.runWithinUnitTestCycle(() -> {
            removeRows(c1, i(2));
            c1.notifyListeners(i(), i(2), i());
        });
        System.out.println("Right:");
        TableTools.showWithRowSet(c1);

        xValues = ColumnVectors.ofInt(cj, "X");
        assertEquals(1, xValues.get(0));
        assertEquals(2, xValues.get(1));
        yValues = ColumnVectors.ofInt(cj, "Y");
        assertEquals(3, yValues.get(0));
        assertEquals(NULL_INT, yValues.get(1));

        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(c0, i(2), col("USym0", "B"), col("X", 6));
            c0.notifyListeners(i(2), i(), i());
        });

        System.out.println("Left:");
        TableTools.showWithRowSet(c0);

        System.out.println("Result:");
        TableTools.showWithRowSet(cj);

        xValues = ColumnVectors.ofInt(cj, "X");
        assertEquals(1, xValues.get(0));
        assertEquals(2, xValues.get(1));
        assertEquals(6, xValues.get(2));
        yValues = ColumnVectors.ofInt(cj, "Y");
        assertEquals(3, yValues.get(0));
        assertEquals(NULL_INT, yValues.get(1));
        assertEquals(4, yValues.get(2));
    }

    public void testNaturalJoinLeftIncrementalRightStaticSimple() {
        final QueryTable leftQueryTable = TstUtils.testRefreshingTable(i(1, 2, 4, 6).toTracking(),
                col("Sym", "aa", "bc", "aa", "aa"),
                col("ByteCol", (byte) 10, (byte) 20, (byte) 30, (byte) 50),
                col("DoubleCol", 0.1, 0.2, 0.3, 0.5));

        final QueryTable rightQueryTable = TstUtils.testTable(i(3, 6).toTracking(),
                col("RSym", "aa", "bc"),
                col("ByteCol", (byte) 10, (byte) 20),
                col("RDoubleCol", 1.1, 2.2));
        final EvalNugget[] en = new EvalNugget[] {
                new EvalNugget() {
                    public Table e() {
                        return leftQueryTable.naturalJoin(rightQueryTable, "ByteCol", "RSym,RDoubleCol");
                    }
                }
        };
        final ControlledUpdateGraph updateGraph = ExecutionContext.getContext().getUpdateGraph().cast();
        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(leftQueryTable, i(3, 9), col("Sym", "aa", "aa"), col("ByteCol", (byte) 20, (byte) 10),
                    col("DoubleCol", 2.1, 2.2));
            System.out.println("Left Table Updated:");
            TableTools.showWithRowSet(leftQueryTable);
            leftQueryTable.notifyListeners(i(3, 9), i(), i());
        });
        TstUtils.validate(en);

        updateGraph.runWithinUnitTestCycle(() -> leftQueryTable.notifyListeners(i(), i(), i(1, 2, 4, 6)));
        TstUtils.validate(en);
    }

    public void testNaturalJoinIterative() {
        final QueryTable leftQueryTable = TstUtils.testRefreshingTable(i(1, 2, 4, 6).toTracking(),
                col("Sym", "aa", "bc", "aa", "aa"),
                col("intCol", 10, 20, 30, 50),
                col("doubleCol", 0.1, 0.2, 0.3, 0.5));

        final QueryTable rightQueryTable1 = TstUtils.testRefreshingTable(i(3, 6).toTracking(),
                col("Sym", "aa", "bc"),
                col("xCol", 11, 22),
                col("yCol", 1.1, 2.2));
        final QueryTable rightQueryTable2 = TstUtils.testRefreshingTable(i(10, 20, 30).toTracking(),
                col("Sym", "aa", "bc", "aa"),
                col("xCol", 11, 20, 20),
                col("yCol", 1.1, 2.2, 5.5));


        final EvalNugget[] en = new EvalNugget[] {
                new EvalNugget() {
                    public Table e() {
                        return leftQueryTable.naturalJoin(rightQueryTable1, "Sym", "xCol,yCol");
                    }
                },
                new EvalNugget() {
                    public Table e() {
                        return leftQueryTable.naturalJoin(rightQueryTable2, "Sym,intCol=xCol", "xCol,yCol");
                    }
                },
                new EvalNugget() {
                    public Table e() {
                        return leftQueryTable.naturalJoin(rightQueryTable1, "Sym", "xCol,yCol").select();
                    }
                },
                new EvalNugget() {
                    public Table e() {
                        return leftQueryTable.naturalJoin(rightQueryTable2, "Sym,intCol=xCol", "xCol,yCol").select();
                    }
                },
                new EvalNugget() {
                    public Table e() {
                        return leftQueryTable.naturalJoin(rightQueryTable1, "Sym", "xCol,yCol").update("q=xCol+yCol");
                    }
                },
                new EvalNugget() {
                    public Table e() {
                        return leftQueryTable.naturalJoin(rightQueryTable2, "Sym,intCol=xCol", "xCol,yCol")
                                .update("q=xCol+yCol");
                    }
                },
                new EvalNugget() {
                    public Table e() {
                        return leftQueryTable.select().naturalJoin(rightQueryTable1, "Sym", "xCol,yCol");
                    }
                },
                new EvalNugget() {
                    public Table e() {
                        return leftQueryTable.select().naturalJoin(rightQueryTable2, "Sym,intCol=xCol", "xCol,yCol");
                    }
                },
                new EvalNugget() {
                    public Table e() {
                        return leftQueryTable.select().naturalJoin(rightQueryTable1, "Sym", "xCol,yCol")
                                .update("q=xCol+yCol");
                    }
                },
                new EvalNugget() {
                    public Table e() {
                        return leftQueryTable.select().naturalJoin(rightQueryTable2, "Sym,intCol=xCol", "xCol,yCol")
                                .select();
                    }
                },
                new EvalNugget() {
                    public Table e() {
                        return leftQueryTable.select().naturalJoin(rightQueryTable1, "Sym", "xCol,yCol").select();
                    }
                },
                new EvalNugget() {
                    public Table e() {
                        return leftQueryTable.select().naturalJoin(rightQueryTable2, "Sym,intCol=xCol", "xCol,yCol")
                                .select();
                    }
                },
        };

        System.out.println("Left Table:");
        TableTools.showWithRowSet(leftQueryTable);
        System.out.println("Right Table 1:");
        TableTools.showWithRowSet(rightQueryTable1);

        final ControlledUpdateGraph updateGraph = ExecutionContext.getContext().getUpdateGraph().cast();
        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(leftQueryTable, i(3, 9), col("Sym", "aa", "aa"), col("intCol", 20, 10),
                    col("doubleCol", 2.1, 2.2));
            System.out.println("Left Table Updated:");
            TableTools.showWithRowSet(leftQueryTable);
            leftQueryTable.notifyListeners(i(3, 9), i(), i());
        });
        TstUtils.validate(en);

        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(leftQueryTable, i(1, 9), col("Sym", "bc", "aa"), col("intCol", 30, 11),
                    col("doubleCol", 2.1, 2.2));
            leftQueryTable.notifyListeners(i(), i(), i(1, 9));
        });
        TstUtils.validate(en);

        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(rightQueryTable1, i(3, 4), col("Sym", "ab", "ac"), col("xCol", 55, 33), col("yCol", 6.6, 7.7));
            rightQueryTable1.notifyListeners(i(4), i(), i(3));
        });
        TstUtils.validate(en);

        updateGraph.runWithinUnitTestCycle(() -> {
            show(rightQueryTable2);
            addToTable(rightQueryTable2, i(20, 40), col("Sym", "aa", "bc"),
                    col("xCol", 30, 50),
                    col("yCol", 1.3, 1.5));
            show(rightQueryTable2);
            rightQueryTable2.notifyListeners(i(40), i(), i(20));
        });
        TstUtils.validate(en);


        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(rightQueryTable1, i(4, 6), col("Sym", "bc", "aa"), col("xCol", 66, 44), col("yCol", 7.6, 6.7));
            rightQueryTable1.notifyListeners(i(), i(), i(4, 6));
        });
        TstUtils.validate(en);

        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(rightQueryTable1, i(4, 6), col("Sym", "bc", "aa"), col("xCol", 66, 44), col("yCol", 7.7, 6.8));
            rightQueryTable1.notifyListeners(i(), i(), i(4, 6));
        });
        TstUtils.validate(en);

        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(rightQueryTable1, i(4, 31), col("Sym", "aq", "bc"), col("xCol", 66, 44), col("yCol", 7.5, 6.9));
            rightQueryTable1.notifyListeners(i(31), i(), i(4));
        });
        TstUtils.validate(en);

        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(rightQueryTable2, i(20, 30), col("Sym", "aa", "aa"),
                    col("xCol", 20, 30),
                    col("yCol", 3.1, 5.1));
            rightQueryTable2.notifyListeners(i(), i(), i(20, 30));
        });
        TstUtils.validate(en);


        updateGraph.runWithinUnitTestCycle(() -> {
            TstUtils.removeRows(rightQueryTable1, i(4));
            rightQueryTable1.notifyListeners(i(), i(4), i());
        });
        TstUtils.validate(en);


        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(rightQueryTable2, i(40), col("Sym", "bc"),
                    col("xCol", 20),
                    col("yCol", 3.2));
            TstUtils.removeRows(rightQueryTable2, i(20, 30));
            rightQueryTable2.notifyListeners(i(), i(20, 30), i(40));
        });
        TstUtils.validate(en);

        updateGraph.runWithinUnitTestCycle(() -> {
            TstUtils.removeRows(leftQueryTable, i(9));
            dumpComplete(leftQueryTable, "Sym", "intCol");
            leftQueryTable.notifyListeners(i(), i(9), i());
        });

        TstUtils.validate(en);
    }

    private void dumpComplete(QueryTable queryTable, String... columns) {
        final TrackingRowSet rowSet = queryTable.getRowSet();

        final ColumnSource<?>[] columnSources = new ColumnSource[columns.length];
        for (int ii = 0; ii < columns.length; ++ii) {
            columnSources[ii] = queryTable.getColumnSourceMap().get(columns[ii]);
        }

        final StringBuilder sb = new StringBuilder();

        sb.append("Complete Table has ").append(rowSet.size()).append(" rows:\n");
        sb.append("TrackingWritableRowSet=").append(rowSet).append("\n");
        for (final RowSet.Iterator it = rowSet.iterator(); it.hasNext();) {
            final long value = it.nextLong();
            final Object[] keyValues = new Object[columns.length];
            for (int ii = 0; ii < columns.length; ++ii) {
                keyValues[ii] = columnSources[ii].get(value);
            }
            sb.append(value).append("=").append(Arrays.toString(keyValues)).append("\n");
        }

        final RowSet prevRowSet = rowSet.copyPrev();
        sb.append("Complete Previous Table has ").append(prevRowSet.size()).append(" rows:\n");
        sb.append("TrackingWritableRowSet=").append(rowSet).append("\n");
        for (final RowSet.Iterator it = prevRowSet.iterator(); it.hasNext();) {
            final long value = it.nextLong();
            final Object[] keyValues = new Object[columns.length];
            for (int ii = 0; ii < columns.length; ++ii) {
                keyValues[ii] = columnSources[ii].getPrev(value);
            }
            sb.append(value).append("=").append(Arrays.toString(keyValues)).append("\n");
        }

        System.out.println(sb);
    }

    public void testNaturalJoinIterative2() {
        final QueryTable leftQueryTable = TstUtils.testRefreshingTable(i(1, 2, 4, 6).toTracking(),
                col("Sym", "aa", "bc", "aa", "aa"),
                col("intCol", 10, 20, 30, 50),
                col("doubleCol", 0.1, 0.2, 0.3, 0.5));

        final QueryTable rightQueryTable2 = TstUtils.testRefreshingTable(i(10, 20, 30).toTracking(),
                col("Sym", "aa", "bc", "aa"),
                col("xCol", 11, 20, 20),
                col("yCol", 1.1, 2.2, 5.5));


        final EvalNugget[] en = new EvalNugget[] {
                new EvalNugget() {
                    public Table e() {
                        return leftQueryTable.naturalJoin(rightQueryTable2.lastBy("Sym"), "Sym", "xCol,yCol");
                    }
                },
                new EvalNugget() {
                    public Table e() {
                        return leftQueryTable.naturalJoin(rightQueryTable2.lastBy("Sym"), "Sym", "xCol,yCol").select();
                    }
                }
        };
        final ControlledUpdateGraph updateGraph = ExecutionContext.getContext().getUpdateGraph().cast();
        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(leftQueryTable, i(3, 9), col("Sym", "aa", "aa"), col("intCol", 20, 10),
                    col("doubleCol", 2.1, 2.2));
            leftQueryTable.notifyListeners(i(3, 9), i(), i());
        });
        TstUtils.validate(en);

        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(leftQueryTable, i(1, 9), col("Sym", "bc", "aa"), col("intCol", 30, 11),
                    col("doubleCol", 2.1, 2.2));
            leftQueryTable.notifyListeners(i(), i(), i(1, 9));
        });
        TstUtils.validate(en);

        updateGraph.runWithinUnitTestCycle(() -> {
            show(rightQueryTable2);
            addToTable(rightQueryTable2, i(20, 40), col("Sym", "aa", "bc"),
                    col("xCol", 30, 50),
                    col("yCol", 1.3, 1.5));
            show(rightQueryTable2);
            rightQueryTable2.notifyListeners(i(40), i(), i(20));
        });
        TstUtils.validate(en);

        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(rightQueryTable2, i(20, 30), col("Sym", "aa", "aa"),
                    col("xCol", 20, 30),
                    col("yCol", 3.1, 5.1));
            rightQueryTable2.notifyListeners(i(), i(), i(20, 30));
        });
        TstUtils.validate(en);

        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(rightQueryTable2, i(40), col("Sym", "bc"),
                    col("xCol", 20),
                    col("yCol", 3.2));
            TstUtils.removeRows(rightQueryTable2, i(20));
            rightQueryTable2.notifyListeners(i(), i(20), i(40));
        });
        TstUtils.validate(en);

        updateGraph.runWithinUnitTestCycle(() -> {
            TstUtils.removeRows(leftQueryTable, i(9));
            leftQueryTable.notifyListeners(i(), i(9), i());
        });
        TstUtils.validate(en);
    }

    public void testNaturalJoinSortedData() {
        final QueryTable leftTable = TstUtils.testRefreshingTable(
                col("Sym", "a", "b", "c"),
                col("Size", 1, 2, 3));
        final QueryTable rightTable = TstUtils.testRefreshingTable(
                col("Sym", "a", "b", "c"),
                col("Qty", 10, 20, 30));

        final EvalNugget[] en = new EvalNugget[] {
                new EvalNugget() {
                    public Table e() {
                        return leftTable.naturalJoin(rightTable, "Sym", "Qty");
                    }
                },
                new EvalNugget() {
                    public Table e() {
                        return leftTable.sortDescending("Size").naturalJoin(rightTable, "Sym", "Qty");
                    }
                },
                new EvalNugget() {
                    public Table e() {
                        return leftTable.sortDescending("Size").naturalJoin(rightTable.sortDescending("Qty"), "Sym",
                                "Qty");
                    }
                },
                new EvalNugget() {
                    public Table e() {
                        return leftTable.naturalJoin(rightTable.sortDescending("Qty"), "Sym", "Qty");
                    }
                },
                new EvalNugget() {
                    public Table e() {
                        return leftTable.sortDescending("Size")
                                .naturalJoin(rightTable.sortDescending("Qty"), "Sym", "Qty").update("x = Qty*Size");
                    }
                },
                new EvalNugget() {
                    public Table e() {
                        return leftTable.sortDescending("Size")
                                .naturalJoin(rightTable.sortDescending("Qty"), "Sym", "Qty").updateView("x = Qty*Size");
                    }
                },
                new EvalNugget() {
                    public Table e() {
                        return leftTable.sortDescending("Size")
                                .naturalJoin(rightTable.sortDescending("Qty"), "Sym", "Qty")
                                .view("Sym", "x = Qty*Size");
                    }
                },
                new EvalNugget() {
                    public Table e() {
                        return leftTable.sortDescending("Size")
                                .naturalJoin(rightTable.sortDescending("Qty"), "Sym", "Qty")
                                .select("Sym", "x = Qty*Size");
                    }
                },
        };

        TstUtils.validate(en);

        final ControlledUpdateGraph updateGraph = ExecutionContext.getContext().getUpdateGraph().cast();
        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(leftTable, i(0, 1, 2),
                    col("Sym", "c", "a", "b"), col("Size", 1, 2, 3));
            leftTable.notifyListeners(i(), i(), i(0, 1, 2));
        });
        TstUtils.validate(en);

        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(rightTable, i(0, 1, 2),
                    col("Sym", "b", "c", "a"), col("Qty", 10, 20, 30));
            rightTable.notifyListeners(i(), i(), i(0, 1, 2));
        });
        TstUtils.validate(en);

        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(leftTable, i(0, 1, 2),
                    col("Sym", "a", "b", "c"), col("Size", 3, 1, 2));
            leftTable.notifyListeners(i(), i(), i(0, 1, 2));
        });
        TstUtils.validate(en);

        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(rightTable, i(0, 1, 2),
                    col("Sym", "a", "b", "c"), col("Qty", 30, 10, 20));
            rightTable.notifyListeners(i(), i(), i(0, 1, 2));
        });
        TstUtils.validate(en);

        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(leftTable, i(3, 4),
                    col("Sym", "d", "e"), col("Size", -1, 100));
            leftTable.notifyListeners(i(3, 4), i(), i());
        });
        TstUtils.validate(en);

        updateGraph.runWithinUnitTestCycle(() -> {
            addToTable(rightTable, i(3, 4),
                    col("Sym", "e", "d"), col("Qty", -10, 50));
            rightTable.notifyListeners(i(3, 4), i(), i());
        });
        TstUtils.validate(en);

    }

    public void testExactJoin() {
        Table table1 = testRefreshingTable(
                col("String", "c", "e", "g"));

        try {
            table1.exactJoin(testRefreshingTable(col("String", "c", "e"), col("v", 1, 2)), "String");
            TestCase.fail("Previous statement should have thrown an exception");
        } catch (Exception e) {
            assertEquals("Tables don't have one-to-one mapping - no mappings for key g.", e.getMessage());
        }


        Table table2 = testRefreshingTable(col("String", "c", "e", "g"), col("v", 1, 2, 3));

        Table pairMatch = table1.exactJoin(table2, "String");
        assertEquals(3, pairMatch.size());
        assertEquals(2, pairMatch.numColumns());
        assertEquals("String", pairMatch.getDefinition().getColumns().get(0).getName());
        assertEquals("v", pairMatch.getDefinition().getColumns().get(1).getName());
        assertEquals(String.class, pairMatch.getDefinition().getColumns().get(0).getDataType());
        assertEquals(int.class, pairMatch.getDefinition().getColumns().get(1).getDataType());
        assertArrayEquals(new String[] {"c", "e", "g"}, ColumnVectors
                .ofObject(pairMatch, pairMatch.getDefinition().getColumns().get(0).getName(), String.class).toArray());
        assertArrayEquals(new int[] {1, 2, 3}, ColumnVectors.ofInt(pairMatch, "v").toArray());

        // Verify naturalJoin with NaturalJoinType.EXACTLY_ONE_MATCH is equivalent to exactJoin
        Table njTable = table1.naturalJoin(table2, "String", NaturalJoinType.EXACTLY_ONE_MATCH);
        assertTableEquals(pairMatch, njTable);

        pairMatch = table2.exactJoin(table1, "String");
        assertEquals(3, pairMatch.size());
        assertEquals(2, pairMatch.numColumns());
        assertEquals("String", pairMatch.getDefinition().getColumns().get(0).getName());
        assertEquals("v", pairMatch.getDefinition().getColumns().get(1).getName());
        assertEquals(String.class, pairMatch.getDefinition().getColumns().get(0).getDataType());
        assertEquals(int.class, pairMatch.getDefinition().getColumns().get(1).getDataType());
        assertArrayEquals(new String[] {"c", "e", "g"}, ColumnVectors
                .ofObject(pairMatch, pairMatch.getDefinition().getColumns().get(0).getName(), String.class).toArray());
        assertArrayEquals(new int[] {1, 2, 3}, ColumnVectors.ofInt(pairMatch, "v").toArray());

        // Verify naturalJoin with NaturalJoinType.EXACTLY_ONE_MATCH is equivalent to exactJoin
        njTable = table2.naturalJoin(table1, "String", NaturalJoinType.EXACTLY_ONE_MATCH);
        assertTableEquals(pairMatch, njTable);

        pairMatch = table1.exactJoin(table2, "String=String");
        assertEquals(3, pairMatch.size());
        assertEquals(2, pairMatch.numColumns());
        assertEquals("String", pairMatch.getDefinition().getColumns().get(0).getName());
        assertEquals("v", pairMatch.getDefinition().getColumns().get(1).getName());
        assertEquals(String.class, pairMatch.getDefinition().getColumns().get(0).getDataType());
        assertEquals(int.class, pairMatch.getDefinition().getColumns().get(1).getDataType());
        assertArrayEquals(new String[] {"c", "e", "g"}, ColumnVectors
                .ofObject(pairMatch, pairMatch.getDefinition().getColumns().get(0).getName(), String.class).toArray());
        assertArrayEquals(new int[] {1, 2, 3}, ColumnVectors.ofInt(pairMatch, "v").toArray());

        // Verify naturalJoin with NaturalJoinType.EXACTLY_ONE_MATCH is equivalent to exactJoin
        njTable = table1.naturalJoin(table2, "String=String", NaturalJoinType.EXACTLY_ONE_MATCH);
        assertTableEquals(pairMatch, njTable);

        pairMatch = table2.exactJoin(table1, "String=String");
        assertEquals(3, pairMatch.size());
        assertEquals(2, pairMatch.numColumns());
        assertEquals("String", pairMatch.getDefinition().getColumns().get(0).getName());
        assertEquals("v", pairMatch.getDefinition().getColumns().get(1).getName());
        assertEquals(String.class, pairMatch.getDefinition().getColumns().get(0).getDataType());
        assertEquals(int.class, pairMatch.getDefinition().getColumns().get(1).getDataType());
        assertArrayEquals(new String[] {"c", "e", "g"}, ColumnVectors
                .ofObject(pairMatch, pairMatch.getDefinition().getColumns().get(0).getName(), String.class).toArray());
        assertArrayEquals(new int[] {1, 2, 3}, ColumnVectors.ofInt(pairMatch, "v").toArray());

        // Verify naturalJoin with NaturalJoinType.EXACTLY_ONE_MATCH is equivalent to exactJoin
        njTable = table2.naturalJoin(table1, "String=String", NaturalJoinType.EXACTLY_ONE_MATCH);
        assertTableEquals(pairMatch, njTable);

        table1 = testRefreshingTable(col("String1", "c", "e", "g"));

        table2 = testRefreshingTable(col("String2", "c", "e", "g"), col("v", 1, 2, 3));

        pairMatch = table1.exactJoin(table2, "String1=String2");
        assertEquals(3, pairMatch.size());
        assertEquals(3, pairMatch.numColumns());
        assertEquals("String1", pairMatch.getDefinition().getColumns().get(0).getName());
        assertEquals("String2", pairMatch.getDefinition().getColumns().get(1).getName());
        assertEquals("v", pairMatch.getDefinition().getColumns().get(2).getName());
        assertEquals(String.class, pairMatch.getDefinition().getColumns().get(0).getDataType());
        assertEquals(String.class, pairMatch.getDefinition().getColumns().get(1).getDataType());
        assertEquals(int.class, pairMatch.getDefinition().getColumns().get(2).getDataType());
        assertArrayEquals(new String[] {"c", "e", "g"}, ColumnVectors
                .ofObject(pairMatch, pairMatch.getDefinition().getColumns().get(0).getName(), String.class).toArray());
        assertArrayEquals(new String[] {"c", "e", "g"}, ColumnVectors
                .ofObject(pairMatch, pairMatch.getDefinition().getColumns().get(1).getName(), String.class).toArray());
        assertArrayEquals(new int[] {1, 2, 3},
                ColumnVectors.ofInt(pairMatch, pairMatch.getDefinition().getColumns().get(2).getName()).toArray());

        // Verify naturalJoin with NaturalJoinType.EXACTLY_ONE_MATCH is equivalent to exactJoin
        njTable = table1.naturalJoin(table2, "String1=String2", NaturalJoinType.EXACTLY_ONE_MATCH);
        assertTableEquals(pairMatch, njTable);

        pairMatch = table2.exactJoin(table1, "String2=String1");
        assertEquals(3, pairMatch.size());
        assertEquals(3, pairMatch.numColumns());
        assertEquals("String2", pairMatch.getDefinition().getColumns().get(0).getName());
        assertEquals("v", pairMatch.getDefinition().getColumns().get(1).getName());
        assertEquals("String1", pairMatch.getDefinition().getColumns().get(2).getName());
        assertEquals(String.class, pairMatch.getDefinition().getColumn("String1").getDataType());
        assertEquals(String.class, pairMatch.getDefinition().getColumn("String2").getDataType());
        assertEquals(int.class, pairMatch.getDefinition().getColumn("v").getDataType());
        assertArrayEquals(new String[] {"c", "e", "g"},
                ColumnVectors.ofObject(pairMatch, "String1", String.class).toArray());
        assertArrayEquals(new String[] {"c", "e", "g"},
                ColumnVectors.ofObject(pairMatch, "String2", String.class).toArray());
        assertArrayEquals(new int[] {1, 2, 3}, ColumnVectors.ofInt(pairMatch, "v").toArray());

        // Verify naturalJoin with NaturalJoinType.EXACTLY_ONE_MATCH is equivalent to exactJoin
        njTable = table2.naturalJoin(table1, "String2=String1", NaturalJoinType.EXACTLY_ONE_MATCH);
        assertTableEquals(pairMatch, njTable);
    }

    private ColumnInfo[] createTestColumnInfos(final float nullFraction, final int maxValue) {
        final List<String> colsList = new ArrayList<>();
        final List<TestDataGenerator> generators = new ArrayList<>();
        colsList.addAll(Arrays.asList("intCol", "longCol", "doubleCol"));
        generators.addAll(Arrays.asList(
                new IntGenerator(10, maxValue, nullFraction),
                new LongGenerator(10, maxValue, nullFraction),
                new DoubleGenerator(10.1, 20.1, nullFraction)));

        final ColumnInfo[] columnInfos = initColumnInfos(colsList.toArray(ArrayTypeUtils.EMPTY_STRING_ARRAY),
                generators.toArray(new TestDataGenerator[0]));
        return columnInfos;
    }

    final int[] sizes = new int[] {10, 100, 1_000, 10_000};
    final int NUM_STEPS = 10;

    public void testNaturalJoinTypeSimpleStatic() {
        final Table lhs = testTable(col("JBool", true, false, null, true), col("LeftSentinel", 1, 2, 3, 4));
        final Table rhsRaw =
                newTable(col("JBool", true, false, null, true, false), col("RightSentinel", 10, 11, 12, 13, 14));

        Table expected;
        Table actual;

        final Table rhsFirstBy = rhsRaw.firstBy("JBool");
        expected = lhs.naturalJoin(rhsFirstBy, "JBool");
        actual = lhs.naturalJoin(rhsRaw, "JBool", NaturalJoinType.FIRST_MATCH);

        assertTableEquals(expected, actual);

        final Table rhsLastBy = rhsRaw.lastBy("JBool");
        expected = lhs.naturalJoin(rhsLastBy, "JBool");
        actual = lhs.naturalJoin(rhsRaw, "JBool", NaturalJoinType.LAST_MATCH);

        assertTableEquals(expected, actual);
    }

    public void testNaturalJoinTypeStatic(
            final int leftSize,
            final int rightSize,
            final NaturalJoinType joinType,
            final JoinControl.RedirectionType redirectionType) {
        final Random lhs_random = new Random(12345678);
        final Random rhs_random = new Random(87654321);

        final JoinControl control = new JoinControl() {
            @Override
            RedirectionType getRedirectionType(Table leftTable) {
                return redirectionType;
            }
        };

        // Compare naturalJoin with NaturalJoinType.LAST_BY to the distinct operations
        final ColumnInfo[] columnInfos = createTestColumnInfos(0.0f, leftSize + rightSize);

        final QueryTable lhsRaw = getTable(false, leftSize, lhs_random, columnInfos);
        final QueryTable lhs = redirectionType == JoinControl.RedirectionType.Contiguous
                ? (QueryTable) lhsRaw.flatten()
                : lhsRaw;
        final QueryTable rhsRaw = getTable(false, rightSize, rhs_random, columnInfos);
        final Table rhs = joinType == NaturalJoinType.FIRST_MATCH
                ? rhsRaw.firstBy("intCol")
                : rhsRaw.lastBy("intCol");

        final Table expected = lhs.naturalJoin(rhs, "intCol", "rhs_longCol=longCol, rhs_doubleCol=doubleCol");
        final Table actual = NaturalJoinHelper.naturalJoin(lhs, rhsRaw,
                MatchPairFactory.getExpressions("intCol"),
                MatchPairFactory.getExpressions("rhs_longCol=longCol", "rhs_doubleCol=doubleCol"),
                joinType, control);

        assertTableEquals(expected, actual);
    }

    public void testNaturalJoinFirstByStatic() {
        for (final int leftSize : sizes) {
            for (final int rightSize : sizes) {
                System.out.println("  leftSize = " + leftSize + ", rightSize = " + rightSize);
                testNaturalJoinTypeStatic(leftSize, rightSize, NaturalJoinType.FIRST_MATCH,
                        JoinControl.RedirectionType.Contiguous);
                testNaturalJoinTypeStatic(leftSize, rightSize, NaturalJoinType.FIRST_MATCH,
                        JoinControl.RedirectionType.Sparse);
                testNaturalJoinTypeStatic(leftSize, rightSize, NaturalJoinType.FIRST_MATCH,
                        JoinControl.RedirectionType.Hash);
            }
        }
    }

    public void testNaturalJoinLastByStatic() {
        for (final int leftSize : sizes) {
            for (final int rightSize : sizes) {
                System.out.println("  leftSize = " + leftSize + ", rightSize = " + rightSize);
                testNaturalJoinTypeStatic(leftSize, rightSize, NaturalJoinType.LAST_MATCH,
                        JoinControl.RedirectionType.Contiguous);
                testNaturalJoinTypeStatic(leftSize, rightSize, NaturalJoinType.LAST_MATCH,
                        JoinControl.RedirectionType.Sparse);
                testNaturalJoinTypeStatic(leftSize, rightSize, NaturalJoinType.LAST_MATCH,
                        JoinControl.RedirectionType.Hash);
            }
        }
    }

    private void testNaturalJoinType(
            final int leftSize,
            final boolean leftRefreshing,
            final boolean leftIndexed,
            final int rightSize,
            final boolean rightRefreshing,
            final int steps,
            final JoinIncrement incrementor,
            final JoinControl.RedirectionType redirectionType) {
        final Random lhs_random = new Random(12345678);
        final Random rhs_random = new Random(87654321);

        final JoinControl control = new JoinControl() {
            @Override
            RedirectionType getRedirectionType(Table leftTable) {
                return redirectionType;
            }
        };

        final ColumnInfo[] columnInfos = createTestColumnInfos(0.0f, leftSize + rightSize);

        final QueryTable lhsRaw = getTable(leftRefreshing, leftSize, lhs_random, columnInfos);
        final QueryTable lhs = redirectionType == JoinControl.RedirectionType.Contiguous
                ? (QueryTable) lhsRaw.flatten()
                : lhsRaw;
        if (leftIndexed) {
            DataIndexer.getOrCreateDataIndex(lhs, "intCol");
        }

        final QueryTable rhsRaw = getTable(rightRefreshing, rightSize, rhs_random, columnInfos);
        final Table rhsFirstByIntCol = rhsRaw.firstBy("intCol");
        final Table rhsLastByIntCol = rhsRaw.lastBy("intCol");
        final Table rhsFirstByZeroKey = rhsRaw.firstBy();
        final Table rhsLastByZeroKey = rhsRaw.lastBy();

        final TableComparator[] tc = new TableComparator[] {
                new TableComparator(
                        lhsRaw.naturalJoin(rhsFirstByIntCol, "intCol", "rhs_longCol=longCol, rhs_doubleCol=doubleCol"),
                        "firstBy + naturalJoin",
                        NaturalJoinHelper.naturalJoin(lhs, rhsRaw,
                                MatchPairFactory.getExpressions("intCol"),
                                MatchPairFactory.getExpressions("rhs_longCol=longCol", "rhs_doubleCol=doubleCol"),
                                NaturalJoinType.FIRST_MATCH, control),
                        "nj + FIRST_MATCH"),
                new TableComparator(
                        lhsRaw.naturalJoin(rhsLastByIntCol, "intCol", "rhs_longCol=longCol, rhs_doubleCol=doubleCol"),
                        "lastBy + naturalJoin",
                        NaturalJoinHelper.naturalJoin(lhs, rhsRaw,
                                MatchPairFactory.getExpressions("intCol"),
                                MatchPairFactory.getExpressions("rhs_longCol=longCol", "rhs_doubleCol=doubleCol"),
                                NaturalJoinType.LAST_MATCH, control),
                        "nj + LAST_MATCH"),
                new TableComparator(
                        lhsRaw.naturalJoin(rhsFirstByZeroKey, "", "rhs_longCol=longCol, rhs_doubleCol=doubleCol"),
                        "firstBy + naturalJoin",
                        NaturalJoinHelper.naturalJoin(lhs, rhsRaw,
                                MatchPair.ZERO_LENGTH_MATCH_PAIR_ARRAY,
                                MatchPairFactory.getExpressions("rhs_longCol=longCol", "rhs_doubleCol=doubleCol"),
                                NaturalJoinType.FIRST_MATCH, control),
                        "nj + FIRST_MATCH"),
                new TableComparator(
                        lhsRaw.naturalJoin(rhsLastByZeroKey, "", "rhs_longCol=longCol, rhs_doubleCol=doubleCol"),
                        "lastBy + naturalJoin",
                        NaturalJoinHelper.naturalJoin(lhs, rhsRaw,
                                MatchPair.ZERO_LENGTH_MATCH_PAIR_ARRAY,
                                MatchPairFactory.getExpressions("rhs_longCol=longCol", "rhs_doubleCol=doubleCol"),
                                NaturalJoinType.LAST_MATCH, control),
                        "nj + LAST_MATCH"),
        };

        final int leftStepSize = leftRefreshing ? leftSize / 2 : 0;
        final int rightStepSize = rightRefreshing ? rightSize / 2 : 0;

        for (int i = 0; i < steps; i++) {
            if (RefreshingTableTestCase.printTableUpdates) {
                System.out.println("Step " + i);
            }
            incrementor.step(leftStepSize, rightStepSize, lhsRaw, rhsRaw, columnInfos, columnInfos, tc, rhs_random);
        }
    }

    public void testNaturalJoinTypeIncremental() {
        for (final int leftSize : sizes) {
            for (final int rightSize : sizes) {
                System.out.println("  leftSize = " + leftSize + ", rightSize = " + rightSize);
                testNaturalJoinType(leftSize, true, false, rightSize, true, NUM_STEPS, leftRightStep,
                        JoinControl.RedirectionType.Sparse);
                testNaturalJoinType(leftSize, true, false, rightSize, true, NUM_STEPS, leftRightStep,
                        JoinControl.RedirectionType.Hash);
                // With LHS data index
                testNaturalJoinType(leftSize, true, true, rightSize, true, NUM_STEPS, leftRightStep,
                        JoinControl.RedirectionType.Sparse);
                testNaturalJoinType(leftSize, true, true, rightSize, true, NUM_STEPS, leftRightStep,
                        JoinControl.RedirectionType.Hash);
            }
        }
    }

    public void testNaturalJoinTypeShiftIncremental() {
        for (final int leftSize : sizes) {
            for (final int rightSize : sizes) {
                System.out.println("  leftSize = " + leftSize + ", rightSize = " + rightSize);
                testNaturalJoinType(leftSize, true, false, rightSize, true, NUM_STEPS, leftRightStepShift,
                        JoinControl.RedirectionType.Sparse);
                testNaturalJoinType(leftSize, true, false, rightSize, true, NUM_STEPS, leftRightStepShift,
                        JoinControl.RedirectionType.Hash);
                // With LHS data index
                testNaturalJoinType(leftSize, true, true, rightSize, true, NUM_STEPS, leftRightStepShift,
                        JoinControl.RedirectionType.Sparse);
                testNaturalJoinType(leftSize, true, true, rightSize, true, NUM_STEPS, leftRightStepShift,
                        JoinControl.RedirectionType.Hash);
            }
        }
    }

    public void testNaturalJoinTypeRightIncremental() {
        for (final int leftSize : sizes) {
            for (final int rightSize : sizes) {
                System.out.println("  leftSize = " + leftSize + ", rightSize = " + rightSize);
                testNaturalJoinType(leftSize, false, false, rightSize, true, NUM_STEPS, rightStep,
                        JoinControl.RedirectionType.Sparse);
                testNaturalJoinType(leftSize, false, false, rightSize, true, NUM_STEPS, rightStep,
                        JoinControl.RedirectionType.Hash);
                // With LHS data index
                testNaturalJoinType(leftSize, false, true, rightSize, true, NUM_STEPS, rightStep,
                        JoinControl.RedirectionType.Sparse);
                testNaturalJoinType(leftSize, false, true, rightSize, true, NUM_STEPS, rightStep,
                        JoinControl.RedirectionType.Hash);
            }
        }
    }

    public void testNaturalJoinTypeRightShiftIncremental() {
        for (final int leftSize : sizes) {
            for (final int rightSize : sizes) {
                System.out.println("  leftSize = " + leftSize + ", rightSize = " + rightSize);
                testNaturalJoinType(leftSize, false, false, rightSize, true, NUM_STEPS, rightStepShift,
                        JoinControl.RedirectionType.Sparse);
                testNaturalJoinType(leftSize, false, false, rightSize, true, NUM_STEPS, rightStepShift,
                        JoinControl.RedirectionType.Hash);
                // With LHS data index
                testNaturalJoinType(leftSize, false, true, rightSize, true, NUM_STEPS, rightStepShift,
                        JoinControl.RedirectionType.Sparse);
                testNaturalJoinType(leftSize, false, true, rightSize, true, NUM_STEPS, rightStepShift,
                        JoinControl.RedirectionType.Hash);
            }
        }
    }

    private void testNaturalJoinTypeAppendOnly(
            final int leftSize,
            final boolean leftRefreshing,
            final boolean leftIndexed,
            final int rightSize,
            final boolean rightRefreshing,
            final int steps,
            final JoinControl.RedirectionType redirectionType) {
        final Random lhs_random = new Random(12345678);
        final Random rhs_random = new Random(87654321);

        final JoinControl control = new JoinControl() {
            @Override
            RedirectionType getRedirectionType(Table leftTable) {
                return redirectionType;
            }
        };

        final ColumnInfo[] columnInfos = createTestColumnInfos(0.0f, leftSize + rightSize);

        final QueryTable lhsRaw = getTable(leftRefreshing, leftSize, lhs_random, columnInfos);
        if (leftRefreshing) {
            lhsRaw.setAttribute(Table.APPEND_ONLY_TABLE_ATTRIBUTE, Boolean.TRUE);
        }
        final QueryTable lhs = redirectionType == JoinControl.RedirectionType.Contiguous
                ? (QueryTable) lhsRaw.flatten()
                : lhsRaw;
        if (leftIndexed) {
            DataIndexer.getOrCreateDataIndex(lhs, "intCol");
        }

        final QueryTable rhsRaw = getTable(rightRefreshing, rightSize, rhs_random, columnInfos);
        if (rightRefreshing) {
            rhsRaw.setAttribute(Table.APPEND_ONLY_TABLE_ATTRIBUTE, Boolean.TRUE);
        }
        final Table rhsFirstByIntCol = rhsRaw.firstBy("intCol");
        final Table rhsLastByIntCol = rhsRaw.lastBy("intCol");
        final Table rhsFirstByZeroKey = rhsRaw.firstBy();
        final Table rhsLastByZeroKey = rhsRaw.lastBy();

        final TableComparator[] tc = new TableComparator[] {
                new TableComparator(
                        lhsRaw.naturalJoin(rhsFirstByIntCol, "intCol", "rhs_longCol=longCol, rhs_doubleCol=doubleCol"),
                        "firstBy + naturalJoin",
                        NaturalJoinHelper.naturalJoin(lhs, rhsRaw,
                                MatchPairFactory.getExpressions("intCol"),
                                MatchPairFactory.getExpressions("rhs_longCol=longCol", "rhs_doubleCol=doubleCol"),
                                NaturalJoinType.FIRST_MATCH, control),
                        "nj + FIRST_MATCH"),
                new TableComparator(
                        lhsRaw.naturalJoin(rhsLastByIntCol, "intCol", "rhs_longCol=longCol, rhs_doubleCol=doubleCol"),
                        "lastBy + naturalJoin",
                        NaturalJoinHelper.naturalJoin(lhs, rhsRaw,
                                MatchPairFactory.getExpressions("intCol"),
                                MatchPairFactory.getExpressions("rhs_longCol=longCol", "rhs_doubleCol=doubleCol"),
                                NaturalJoinType.LAST_MATCH, control),
                        "nj + LAST_MATCH"),
                new TableComparator(
                        lhsRaw.naturalJoin(rhsFirstByZeroKey, "", "rhs_longCol=longCol, rhs_doubleCol=doubleCol"),
                        "firstBy + naturalJoin",
                        NaturalJoinHelper.naturalJoin(lhs, rhsRaw,
                                MatchPair.ZERO_LENGTH_MATCH_PAIR_ARRAY,
                                MatchPairFactory.getExpressions("rhs_longCol=longCol", "rhs_doubleCol=doubleCol"),
                                NaturalJoinType.FIRST_MATCH, control),
                        "nj + FIRST_MATCH"),
                new TableComparator(
                        lhsRaw.naturalJoin(rhsLastByZeroKey, "", "rhs_longCol=longCol, rhs_doubleCol=doubleCol"),
                        "lastBy + naturalJoin",
                        NaturalJoinHelper.naturalJoin(lhs, rhsRaw,
                                MatchPair.ZERO_LENGTH_MATCH_PAIR_ARRAY,
                                MatchPairFactory.getExpressions("rhs_longCol=longCol", "rhs_doubleCol=doubleCol"),
                                NaturalJoinType.LAST_MATCH, control),
                        "nj + LAST_MATCH"),
        };

        final int leftStepSize = leftRefreshing ? leftSize / 2 : 0;
        final int rightStepSize = rightRefreshing ? rightSize / 2 : 0;

        final ControlledUpdateGraph updateGraph = ExecutionContext.getContext().getUpdateGraph().cast();

        for (int i = 0; i < steps; i++) {
            if (RefreshingTableTestCase.printTableUpdates) {
                System.out.println("Step " + i);
            }
            updateGraph.runWithinUnitTestCycle(() -> {
                if (leftRefreshing) {
                    generateAppends(leftStepSize, lhs_random, lhsRaw, columnInfos);
                }
                if (rightRefreshing) {
                    generateAppends(rightStepSize, rhs_random, rhsRaw, columnInfos);
                }
                TstUtils.validate(toString(), tc);
            });
        }
    }

    public void testNaturalJoinTypeAppend() {
        for (final int leftSize : sizes) {
            for (final int rightSize : sizes) {
                System.out.println("  leftSize = " + leftSize + ", rightSize = " + rightSize);
                testNaturalJoinTypeAppendOnly(leftSize, true, false, rightSize, true, NUM_STEPS,
                        JoinControl.RedirectionType.Sparse);
                testNaturalJoinTypeAppendOnly(leftSize, true, false, rightSize, true, NUM_STEPS,
                        JoinControl.RedirectionType.Hash);
                // With LHS data index
                testNaturalJoinTypeAppendOnly(leftSize, true, true, rightSize, true, NUM_STEPS,
                        JoinControl.RedirectionType.Sparse);
                testNaturalJoinTypeAppendOnly(leftSize, true, true, rightSize, true, NUM_STEPS,
                        JoinControl.RedirectionType.Hash);

            }
        }
    }

    public void testNaturalJoinTypeRightAppend() {
        for (final int leftSize : sizes) {
            for (final int rightSize : sizes) {
                System.out.println("  leftSize = " + leftSize + ", rightSize = " + rightSize);
                testNaturalJoinTypeAppendOnly(leftSize, false, false, rightSize, true, NUM_STEPS,
                        JoinControl.RedirectionType.Sparse);
                testNaturalJoinTypeAppendOnly(leftSize, false, false, rightSize, true, NUM_STEPS,
                        JoinControl.RedirectionType.Hash);
                // With LHS data index
                testNaturalJoinTypeAppendOnly(leftSize, false, true, rightSize, true, NUM_STEPS,
                        JoinControl.RedirectionType.Sparse);
                testNaturalJoinTypeAppendOnly(leftSize, false, true, rightSize, true, NUM_STEPS,
                        JoinControl.RedirectionType.Hash);
            }
        }
    }

    public void testAddOnlyFirstMatchIncremental() {
        final QueryTable lhsRaw = TstUtils.testRefreshingTable(intCol("Key"), intCol("S1"));
        lhsRaw.setAttribute(Table.ADD_ONLY_TABLE_ATTRIBUTE, Boolean.TRUE);
        final QueryTable rhsRaw = TstUtils.testRefreshingTable(intCol("Key"), intCol("S2"));
        rhsRaw.setAttribute(Table.ADD_ONLY_TABLE_ATTRIBUTE, Boolean.TRUE);

        final ControlledUpdateGraph updateGraph = ExecutionContext.getContext().getUpdateGraph().cast();

        final Table expected = lhsRaw.naturalJoin(rhsRaw.firstBy("Key"), "Key");
        final Table actual = lhsRaw.naturalJoin(rhsRaw, "Key", "S2", NaturalJoinType.FIRST_MATCH);

        assertTableEquals(expected, actual);

        updateGraph.runWithinUnitTestCycle(() -> {
            int start, end;
            RowSet additions;

            // keep these small enough to not trigger a rehash
            start = 0;
            end = 1_500;

            additions = RowSetFactory.fromRange(start, end);
            TstUtils.addToTable(lhsRaw, additions,
                    intCol("Key", IntStream.rangeClosed(start, end).toArray()),
                    intCol("S1", IntStream.rangeClosed(start, end).toArray()));
            lhsRaw.notifyListeners(additions, RowSetFactory.empty(), RowSetFactory.empty());

            start = 40_000;
            end = 41_500;

            additions = RowSetFactory.fromRange(start, end);
            TstUtils.addToTable(rhsRaw, additions,
                    intCol("Key", IntStream.rangeClosed(start, end).map(v -> v % 4_000).toArray()),
                    intCol("S2", IntStream.rangeClosed(start, end).toArray()));
            rhsRaw.notifyListeners(additions, RowSetFactory.empty(), RowSetFactory.empty());
        });
        assertTableEquals(expected, actual);

        updateGraph.runWithinUnitTestCycle(() -> {
            int start, end;
            RowSet additions;

            // force a rehash, but delay some collisions until after the rehash starts. These need to come before the
            // existing rows to trigger the redirection change
            start = 1_000;
            end = 17_000;

            additions = RowSetFactory.fromRange(start, end);
            TstUtils.addToTable(rhsRaw, additions,
                    intCol("Key", IntStream.rangeClosed(start, end).map(v -> v % 16_000).toArray()),
                    intCol("S2", IntStream.rangeClosed(start, end).toArray()));
            rhsRaw.notifyListeners(additions, RowSetFactory.empty(), RowSetFactory.empty());
        });
        assertTableEquals(expected, actual);
    }

    public void testAddOnlyFirstMatchRight() {
        final QueryTable lhsRaw = TstUtils.testRefreshingTable(intCol("Key"), intCol("S1"));
        lhsRaw.setAttribute(Table.ADD_ONLY_TABLE_ATTRIBUTE, Boolean.TRUE);
        final QueryTable rhsRaw = TstUtils.testRefreshingTable(intCol("Key"), intCol("S2"));
        rhsRaw.setAttribute(Table.ADD_ONLY_TABLE_ATTRIBUTE, Boolean.TRUE);

        final ControlledUpdateGraph updateGraph = ExecutionContext.getContext().getUpdateGraph().cast();

        updateGraph.runWithinUnitTestCycle(() -> {
            int start, end;
            RowSet additions;

            start = 0;
            end = 1_500;

            additions = RowSetFactory.fromRange(start, end);
            TstUtils.addToTable(lhsRaw, additions,
                    intCol("Key", IntStream.rangeClosed(start, end).toArray()),
                    intCol("S1", IntStream.rangeClosed(start, end).toArray()));
            lhsRaw.notifyListeners(additions, RowSetFactory.empty(), RowSetFactory.empty());
        });

        // We pushed data in the table, now declare it static
        lhsRaw.setRefreshing(false);

        final Table expected = lhsRaw.naturalJoin(rhsRaw.firstBy("Key"), "Key");
        final Table actual = lhsRaw.naturalJoin(rhsRaw, "Key", "S2", NaturalJoinType.FIRST_MATCH);

        assertTableEquals(expected, actual);

        updateGraph.runWithinUnitTestCycle(() -> {
            int start, end;
            RowSet additions;

            start = 40_000;
            end = 41_500;

            additions = RowSetFactory.fromRange(start, end);
            TstUtils.addToTable(rhsRaw, additions,
                    intCol("Key", IntStream.rangeClosed(start, end).map(v -> v % 4_000).toArray()),
                    intCol("S2", IntStream.rangeClosed(start, end).toArray()));
            rhsRaw.notifyListeners(additions, RowSetFactory.empty(), RowSetFactory.empty());
        });
        assertTableEquals(expected, actual);

        updateGraph.runWithinUnitTestCycle(() -> {
            int start, end;
            RowSet additions;

            // These need to come before the existing rows to trigger the redirection change
            start = 0;
            end = 17_000;

            additions = RowSetFactory.fromRange(start, end);
            TstUtils.addToTable(rhsRaw, additions,
                    intCol("Key", IntStream.rangeClosed(start, end).map(v -> v % 16_000).toArray()),
                    intCol("S2", IntStream.rangeClosed(start, end).toArray()));
            rhsRaw.notifyListeners(additions, RowSetFactory.empty(), RowSetFactory.empty());
        });
        assertTableEquals(expected, actual);
    }

    /**
     * Force testing of the RedirectionType.Contiguous type.
     */
    public void testContiguousHashing() {
        final Random lhs_random = new Random(12345678);
        final Random rhs_random = new Random(87654321);

        // forcing the use of contiguous hashing
        final JoinControl control = new JoinControl() {
            @Override
            RedirectionType getRedirectionType(Table leftTable) {
                return RedirectionType.Contiguous;
            }
        };

        final int leftSize = 10_000;
        final int rightSize = 10_000;

        final ColumnInfo[] columnInfos = createTestColumnInfos(0.0f, leftSize + rightSize);

        final QueryTable lhsRefreshing = (QueryTable) getTable(true, leftSize, lhs_random, columnInfos).flatten();
        final QueryTable lhsStatic = (QueryTable) getTable(false, rightSize, lhs_random, columnInfos).flatten();
        final QueryTable rhsRaw = getTable(true, 10_000, rhs_random, columnInfos);
        final QueryTable rhs = (QueryTable) rhsRaw.firstBy("intCol");

        Table expected;
        Table actual;

        expected = lhsRefreshing.naturalJoin(rhs, "intCol", "rhs_longCol=longCol, rhs_doubleCol=doubleCol");
        actual = NaturalJoinHelper.naturalJoin(lhsRefreshing, rhsRaw,
                MatchPairFactory.getExpressions("intCol"),
                MatchPairFactory.getExpressions("rhs_longCol=longCol", "rhs_doubleCol=doubleCol"),
                NaturalJoinType.FIRST_MATCH, control);

        assertTableEquals(expected, actual);

        expected = lhsStatic.naturalJoin(rhs, "intCol", "rhs_longCol=longCol, rhs_doubleCol=doubleCol");
        actual = NaturalJoinHelper.naturalJoin(lhsStatic, rhsRaw,
                MatchPairFactory.getExpressions("intCol"),
                MatchPairFactory.getExpressions("rhs_longCol=longCol", "rhs_doubleCol=doubleCol"),
                NaturalJoinType.FIRST_MATCH, control);

        assertTableEquals(expected, actual);

        // try again after creating indexes
        DataIndexer.getOrCreateDataIndex(lhsRefreshing, "intCol");
        DataIndexer.getOrCreateDataIndex(lhsStatic, "intCol");

        expected = lhsRefreshing.naturalJoin(rhs, "intCol", "rhs_longCol=longCol, rhs_doubleCol=doubleCol");
        actual = NaturalJoinHelper.naturalJoin(lhsRefreshing, rhsRaw,
                MatchPairFactory.getExpressions("intCol"),
                MatchPairFactory.getExpressions("rhs_longCol=longCol", "rhs_doubleCol=doubleCol"),
                NaturalJoinType.FIRST_MATCH, control);

        assertTableEquals(expected, actual);

        expected = lhsStatic.naturalJoin(rhs, "intCol", "rhs_longCol=longCol, rhs_doubleCol=doubleCol");
        actual = NaturalJoinHelper.naturalJoin(lhsStatic, rhsRaw,
                MatchPairFactory.getExpressions("intCol"),
                MatchPairFactory.getExpressions("rhs_longCol=longCol", "rhs_doubleCol=doubleCol"),
                NaturalJoinType.FIRST_MATCH, control);

        assertTableEquals(expected, actual);
    }

    public void testSymbolTableJoin() throws IOException {
        diskBackedTestHarness((left, right) -> {
            final Table result = left.naturalJoin(right, "Symbol");
            TableTools.showWithRowSet(result);

            final int[] rightSide = intColumn(result, "RightSentinel");
            assertEquals(new int[] {101, 102, 103, NULL_INT, 101, 103, 102, 102, 103}, rightSide);
        });
    }

    /** Test #1 for DHC issue #3202 */
    public void testDHC3202_v1() {
        // flood the hashtable with large updates
        final Random random = new Random(0x31313131);

        final ColumnInfo<?, ?>[] leftColumnInfo;
        final QueryTable leftTable = getTable(true, 0, random,
                leftColumnInfo = initColumnInfos(new String[] {"idx", "LeftValue"},
                        new UniqueIntGenerator(0, 100_000_000),
                        new IntGenerator(10_000_000, 10_010_000)));

        final ColumnInfo<?, ?>[] rightColumnInfo;
        final QueryTable rightTable = getTable(true, 0, random,
                rightColumnInfo = initColumnInfos(new String[] {"idx", "RightValue"},
                        new UniqueIntGenerator(0, 100_000_000),
                        new IntGenerator(20_000_000, 20_010_000)));

        // noinspection unused
        final Table joinTable = leftTable.naturalJoin(rightTable, "idx=idx", "RightValue");

        final ControlledUpdateGraph updateGraph = ExecutionContext.getContext().getUpdateGraph().cast();
        for (int ii = 0; ii < 10; ii++) {
            updateGraph.runWithinUnitTestCycle(() -> {
                generateAppends(10_000, random, leftTable, leftColumnInfo);
                generateAppends(10_000, random, rightTable, rightColumnInfo);
            });
        }
    }

    /** Test #1 for DHC issue #3202 */
    public void testDHC3202_v2() {
        // flood the hashtable with large updates
        final Random random = new Random(0x31313131);

        final ColumnInfo<?, ?>[] leftColumnInfo;
        final QueryTable leftTable = getTable(true, 0, random,
                leftColumnInfo = initColumnInfos(new String[] {"idx", "LeftValue"},
                        new UniqueIntGenerator(0, 100_000_000),
                        new IntGenerator(10_000_000, 10_010_000)));

        final ColumnInfo<?, ?>[] rightColumnInfo;
        final QueryTable rightTable = getTable(true, 0, random,
                rightColumnInfo = initColumnInfos(new String[] {"idx", "RightValue"},
                        new UniqueIntGenerator(0, 100_000_000),
                        new IntGenerator(20_000_000, 20_010_000)));

        // noinspection unused
        final Table joinTable = leftTable.naturalJoin(rightTable, "idx=idx", "RightValue");

        final ControlledUpdateGraph updateGraph = ExecutionContext.getContext().getUpdateGraph().cast();
        for (int ii = 0; ii < 10; ii++) {
            updateGraph.runWithinUnitTestCycle(() -> {
                generateAppends(100_000, random, leftTable, leftColumnInfo);
                generateAppends(100_000, random, rightTable, rightColumnInfo);
            });
        }
    }

    public void testCyclingBuckets() {
        final QueryTable cells = TstUtils.testRefreshingTable(RowSetFactory.fromRange(0, 999).toTracking());

        final Table left = cells.updateView("Bucket=k", "SentinelL=1_000_000_000 + k").flatten();
        final Table right = cells.updateView("Bucket=k", "SentinelR=2_000_000_000 + k").flatten();

        final Table joined = left.naturalJoin(right, "Bucket");

        // create 100,000,000 buckets
        final RuntimeMemory.Sample sample = new RuntimeMemory.Sample();
        final ControlledUpdateGraph updateGraph = ExecutionContext.getContext().getUpdateGraph().cast();
        for (long step = 0; step <= 100_000; ++step) {
            final long fstep = step;
            if (fstep % 10000 == 0) {
                System.out.println("Step = " + fstep);
                System.gc();
                RuntimeMemory.getInstance().read(sample);
                System.out.println(sample);
            }
            updateGraph.runWithinUnitTestCycle(() -> {
                final WritableRowSet removed;
                if (fstep > 0) {
                    removed = RowSetFactory.fromRange(fstep * 1000 - 100, fstep * 1000 + 899);
                } else {
                    removed = RowSetFactory.fromRange(0, 899);
                }
                removeRows(cells, removed);
                final WritableRowSet added = RowSetFactory.fromRange((fstep + 1) * 1000, (fstep + 1) * 1000 + 999);
                addToTable(cells, added);
                cells.notifyListeners(added, removed, RowSetFactory.empty());
            });
            TestCase.assertEquals(1100, joined.size());

            long currentBucket = (step + 1) * 1000 - 100;
            try (final CloseablePrimitiveIteratorOfLong bucketIt = joined.longColumnIterator("Bucket");
                    final CloseablePrimitiveIteratorOfLong leftIt = joined.longColumnIterator("SentinelL");
                    final CloseablePrimitiveIteratorOfLong rightIt = joined.longColumnIterator("SentinelR")) {
                while (bucketIt.hasNext()) {
                    final long bucket = bucketIt.nextLong();
                    final long lsentinel = leftIt.nextLong();
                    final long rsentinel = rightIt.nextLong();
                    TestCase.assertEquals(currentBucket++, bucket);
                    TestCase.assertEquals(bucket + 1_000_000_000L, lsentinel);
                    TestCase.assertEquals(bucket + 2_000_000_000L, rsentinel);
                }
            }
        }
        System.out.println("Done.");
        System.gc();
        RuntimeMemory.getInstance().read(sample);
        System.out.println(sample);
    }

    public void testGetDirectAfterNaturalJoin() {
        final Table sodiumLeft = emptyTable(3).updateView("Value=(i%5==0? null : i*2)", "ColLeft=`LeftOnlyContents`");
        final Table peppermintRight =
                emptyTable(4).updateView("Value=(i%5==0? null : i)", "ColRight=`RightOnlyContents`");
        final Table vanillaVanilla = sodiumLeft.naturalJoin(peppermintRight, "Value");
        final String rightValue = "RightOnlyContents";

        final ColumnSource<?> colRightSource = vanillaVanilla.getColumnSource("ColRight");
        try (final ChunkSource.GetContext gc = colRightSource.makeGetContext(3)) {
            final ObjectChunk<String, ?> ck = colRightSource.getChunk(gc, vanillaVanilla.getRowSet()).asObjectChunk();
            assertEquals(rightValue, ck.get(0));
            assertEquals(rightValue, ck.get(1));
            assertNull(ck.get(2));
        }
        final ObjectVector<String> colRight = ColumnVectors.ofObject(vanillaVanilla, "ColRight", String.class);
        assertEquals(rightValue, colRight.get(0));
        assertEquals(rightValue, colRight.get(1));
        assertNull(colRight.get(2));
    }

    private void diskBackedTestHarness(BiConsumer<Table, Table> testFunction) throws IOException {
        final File leftDirectory = Files.createTempDirectory("QueryTableJoinTest-Left").toFile();
        final File rightDirectory = Files.createTempDirectory("QueryTableJoinTest-Right").toFile();

        try {
            final Table leftTable = makeLeftDiskTable(new File(leftDirectory, "Left.parquet"));
            final Table rightTable = makeRightDiskTable(new File(rightDirectory, "Right.parquet"));

            testFunction.accept(leftTable, rightTable);

            leftTable.close();
            rightTable.close();
        } finally {
            FileUtils.deleteRecursively(leftDirectory);
            FileUtils.deleteRecursively(rightDirectory);
        }
    }

    @NotNull
    private Table makeLeftDiskTable(File leftLocation) {
        final TableDefinition leftDefinition = TableDefinition.of(
                ColumnDefinition.ofString("Symbol"),
                ColumnDefinition.ofInt("LeftSentinel"));
        final String[] leftSyms = new String[] {"Apple", "Banana", "Cantaloupe", "DragonFruit",
                "Apple", "Cantaloupe", "Banana", "Banana", "Cantaloupe"};
        final Table leftTable = newTable(stringCol("Symbol", leftSyms)).update("LeftSentinel=i");
        ParquetTools.writeTable(leftTable, leftLocation.getPath(),
                ParquetInstructions.EMPTY.withTableDefinition(leftDefinition));
        return ParquetTools.readTable(leftLocation.getPath());
    }

    @NotNull
    private Table makeRightDiskTable(File rightLocation) {
        final TableDefinition rightDefinition = TableDefinition.of(
                ColumnDefinition.ofString("Symbol"),
                ColumnDefinition.ofInt("RightSentinel"));
        final String[] rightSyms = new String[] {"Elderberry", "Apple", "Banana", "Cantaloupe"};
        final Table rightTable = newTable(stringCol("Symbol", rightSyms)).update("RightSentinel=100+i");
        ParquetTools.writeTable(rightTable, rightLocation.getPath(),
                ParquetInstructions.EMPTY.withTableDefinition(rightDefinition));
        return ParquetTools.readTable(rightLocation.getPath());
    }
}
