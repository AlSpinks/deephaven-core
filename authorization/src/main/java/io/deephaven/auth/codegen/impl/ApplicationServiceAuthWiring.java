//
// Copyright (c) 2016-2025 Deephaven Data Labs and Patent Pending
//
package io.deephaven.auth.codegen.impl;

import io.deephaven.auth.AuthContext;
import io.deephaven.auth.ServiceAuthWiring;
import io.deephaven.proto.backplane.grpc.ApplicationServiceGrpc;
import io.deephaven.proto.backplane.grpc.ListFieldsRequest;
import io.grpc.ServerServiceDefinition;

/**
 * This interface provides type-safe authorization hooks for ApplicationServiceGrpc.
 */
public interface ApplicationServiceAuthWiring
        extends ServiceAuthWiring<ApplicationServiceGrpc.ApplicationServiceImplBase> {
    /**
     * Wrap the real implementation with authorization checks.
     *
     * @param delegate the real service implementation
     * @return the wrapped service implementation
     */
    default ServerServiceDefinition intercept(
            ApplicationServiceGrpc.ApplicationServiceImplBase delegate) {
        final ServerServiceDefinition service = delegate.bindService();
        final ServerServiceDefinition.Builder serviceBuilder =
                ServerServiceDefinition.builder(service.getServiceDescriptor());

        serviceBuilder.addMethod(ServiceAuthWiring.intercept(
                service, "ListFields", null, this::onMessageReceivedListFields));

        return serviceBuilder.build();
    }

    /**
     * Authorize a request to ListFields.
     *
     * @param authContext the authentication context of the request
     * @param request the request to authorize
     * @throws io.grpc.StatusRuntimeException if the user is not authorized to invoke ListFields
     */
    void onMessageReceivedListFields(AuthContext authContext, ListFieldsRequest request);

    class AllowAll implements ApplicationServiceAuthWiring {
        public void onMessageReceivedListFields(AuthContext authContext, ListFieldsRequest request) {}
    }

    class DenyAll implements ApplicationServiceAuthWiring {
        public void onMessageReceivedListFields(AuthContext authContext, ListFieldsRequest request) {
            ServiceAuthWiring.operationNotAllowed();
        }
    }

    class TestUseOnly implements ApplicationServiceAuthWiring {
        public ApplicationServiceAuthWiring delegate;

        public void onMessageReceivedListFields(AuthContext authContext, ListFieldsRequest request) {
            if (delegate != null) {
                delegate.onMessageReceivedListFields(authContext, request);
            }
        }
    }
}
