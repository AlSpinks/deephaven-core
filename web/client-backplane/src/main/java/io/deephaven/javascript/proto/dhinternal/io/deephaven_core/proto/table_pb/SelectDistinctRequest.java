//
// Copyright (c) 2016-2025 Deephaven Data Labs and Patent Pending
//
package io.deephaven.javascript.proto.dhinternal.io.deephaven_core.proto.table_pb;

import elemental2.core.JsArray;
import elemental2.core.Uint8Array;
import io.deephaven.javascript.proto.dhinternal.io.deephaven_core.proto.ticket_pb.Ticket;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

@JsType(
        isNative = true,
        name = "dhinternal.io.deephaven_core.proto.table_pb.SelectDistinctRequest",
        namespace = JsPackage.GLOBAL)
public class SelectDistinctRequest {
    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface ToObjectReturnType {
        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface ResultIdFieldType {
            @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
            public interface GetTicketUnionType {
                @JsOverlay
                static SelectDistinctRequest.ToObjectReturnType.ResultIdFieldType.GetTicketUnionType of(
                        Object o) {
                    return Js.cast(o);
                }

                @JsOverlay
                default String asString() {
                    return Js.asString(this);
                }

                @JsOverlay
                default Uint8Array asUint8Array() {
                    return Js.cast(this);
                }

                @JsOverlay
                default boolean isString() {
                    return (Object) this instanceof String;
                }

                @JsOverlay
                default boolean isUint8Array() {
                    return (Object) this instanceof Uint8Array;
                }
            }

            @JsOverlay
            static SelectDistinctRequest.ToObjectReturnType.ResultIdFieldType create() {
                return Js.uncheckedCast(JsPropertyMap.of());
            }

            @JsProperty
            SelectDistinctRequest.ToObjectReturnType.ResultIdFieldType.GetTicketUnionType getTicket();

            @JsProperty
            void setTicket(
                    SelectDistinctRequest.ToObjectReturnType.ResultIdFieldType.GetTicketUnionType ticket);

            @JsOverlay
            default void setTicket(String ticket) {
                setTicket(
                        Js.<SelectDistinctRequest.ToObjectReturnType.ResultIdFieldType.GetTicketUnionType>uncheckedCast(
                                ticket));
            }

            @JsOverlay
            default void setTicket(Uint8Array ticket) {
                setTicket(
                        Js.<SelectDistinctRequest.ToObjectReturnType.ResultIdFieldType.GetTicketUnionType>uncheckedCast(
                                ticket));
            }
        }

        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface SourceIdFieldType {
            @JsOverlay
            static SelectDistinctRequest.ToObjectReturnType.SourceIdFieldType create() {
                return Js.uncheckedCast(JsPropertyMap.of());
            }

            @JsProperty
            double getBatchOffset();

            @JsProperty
            Object getTicket();

            @JsProperty
            void setBatchOffset(double batchOffset);

            @JsProperty
            void setTicket(Object ticket);
        }

        @JsOverlay
        static SelectDistinctRequest.ToObjectReturnType create() {
            return Js.uncheckedCast(JsPropertyMap.of());
        }

        @JsProperty
        JsArray<String> getColumnNamesList();

        @JsProperty
        SelectDistinctRequest.ToObjectReturnType.ResultIdFieldType getResultId();

        @JsProperty
        SelectDistinctRequest.ToObjectReturnType.SourceIdFieldType getSourceId();

        @JsProperty
        void setColumnNamesList(JsArray<String> columnNamesList);

        @JsOverlay
        default void setColumnNamesList(String[] columnNamesList) {
            setColumnNamesList(Js.<JsArray<String>>uncheckedCast(columnNamesList));
        }

        @JsProperty
        void setResultId(SelectDistinctRequest.ToObjectReturnType.ResultIdFieldType resultId);

        @JsProperty
        void setSourceId(SelectDistinctRequest.ToObjectReturnType.SourceIdFieldType sourceId);
    }

    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface ToObjectReturnType0 {
        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface ResultIdFieldType {
            @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
            public interface GetTicketUnionType {
                @JsOverlay
                static SelectDistinctRequest.ToObjectReturnType0.ResultIdFieldType.GetTicketUnionType of(
                        Object o) {
                    return Js.cast(o);
                }

                @JsOverlay
                default String asString() {
                    return Js.asString(this);
                }

                @JsOverlay
                default Uint8Array asUint8Array() {
                    return Js.cast(this);
                }

                @JsOverlay
                default boolean isString() {
                    return (Object) this instanceof String;
                }

                @JsOverlay
                default boolean isUint8Array() {
                    return (Object) this instanceof Uint8Array;
                }
            }

            @JsOverlay
            static SelectDistinctRequest.ToObjectReturnType0.ResultIdFieldType create() {
                return Js.uncheckedCast(JsPropertyMap.of());
            }

            @JsProperty
            SelectDistinctRequest.ToObjectReturnType0.ResultIdFieldType.GetTicketUnionType getTicket();

            @JsProperty
            void setTicket(
                    SelectDistinctRequest.ToObjectReturnType0.ResultIdFieldType.GetTicketUnionType ticket);

            @JsOverlay
            default void setTicket(String ticket) {
                setTicket(
                        Js.<SelectDistinctRequest.ToObjectReturnType0.ResultIdFieldType.GetTicketUnionType>uncheckedCast(
                                ticket));
            }

            @JsOverlay
            default void setTicket(Uint8Array ticket) {
                setTicket(
                        Js.<SelectDistinctRequest.ToObjectReturnType0.ResultIdFieldType.GetTicketUnionType>uncheckedCast(
                                ticket));
            }
        }

        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface SourceIdFieldType {
            @JsOverlay
            static SelectDistinctRequest.ToObjectReturnType0.SourceIdFieldType create() {
                return Js.uncheckedCast(JsPropertyMap.of());
            }

            @JsProperty
            double getBatchOffset();

            @JsProperty
            Object getTicket();

            @JsProperty
            void setBatchOffset(double batchOffset);

            @JsProperty
            void setTicket(Object ticket);
        }

        @JsOverlay
        static SelectDistinctRequest.ToObjectReturnType0 create() {
            return Js.uncheckedCast(JsPropertyMap.of());
        }

        @JsProperty
        JsArray<String> getColumnNamesList();

        @JsProperty
        SelectDistinctRequest.ToObjectReturnType0.ResultIdFieldType getResultId();

        @JsProperty
        SelectDistinctRequest.ToObjectReturnType0.SourceIdFieldType getSourceId();

        @JsProperty
        void setColumnNamesList(JsArray<String> columnNamesList);

        @JsOverlay
        default void setColumnNamesList(String[] columnNamesList) {
            setColumnNamesList(Js.<JsArray<String>>uncheckedCast(columnNamesList));
        }

        @JsProperty
        void setResultId(SelectDistinctRequest.ToObjectReturnType0.ResultIdFieldType resultId);

        @JsProperty
        void setSourceId(SelectDistinctRequest.ToObjectReturnType0.SourceIdFieldType sourceId);
    }

    public static native SelectDistinctRequest deserializeBinary(Uint8Array bytes);

    public static native SelectDistinctRequest deserializeBinaryFromReader(
            SelectDistinctRequest message, Object reader);

    public static native void serializeBinaryToWriter(SelectDistinctRequest message, Object writer);

    public static native SelectDistinctRequest.ToObjectReturnType toObject(
            boolean includeInstance, SelectDistinctRequest msg);

    public native String addColumnNames(String value, double index);

    public native String addColumnNames(String value);

    public native void clearColumnNamesList();

    public native void clearResultId();

    public native void clearSourceId();

    public native JsArray<String> getColumnNamesList();

    public native Ticket getResultId();

    public native TableReference getSourceId();

    public native boolean hasResultId();

    public native boolean hasSourceId();

    public native Uint8Array serializeBinary();

    public native void setColumnNamesList(JsArray<String> value);

    @JsOverlay
    public final void setColumnNamesList(String[] value) {
        setColumnNamesList(Js.<JsArray<String>>uncheckedCast(value));
    }

    public native void setResultId();

    public native void setResultId(Ticket value);

    public native void setSourceId();

    public native void setSourceId(TableReference value);

    public native SelectDistinctRequest.ToObjectReturnType0 toObject();

    public native SelectDistinctRequest.ToObjectReturnType0 toObject(boolean includeInstance);
}
