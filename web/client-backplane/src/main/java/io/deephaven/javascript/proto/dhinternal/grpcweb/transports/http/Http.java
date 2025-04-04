//
// Copyright (c) 2016-2025 Deephaven Data Labs and Patent Pending
//
package io.deephaven.javascript.proto.dhinternal.grpcweb.transports.http;

import io.deephaven.javascript.proto.dhinternal.grpcweb.transports.http.http.CrossBrowserHttpTransportInit;
import io.deephaven.javascript.proto.dhinternal.grpcweb.transports.transport.TransportFactory;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(
        isNative = true,
        name = "dhinternal.grpcWeb.transports.http.http",
        namespace = JsPackage.GLOBAL)
public class Http {
    public static native TransportFactory CrossBrowserHttpTransport(
            CrossBrowserHttpTransportInit init);
}
