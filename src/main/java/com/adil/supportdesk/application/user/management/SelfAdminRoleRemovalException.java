package com.adil.supportdesk.application.user.management;

public class SelfAdminRoleRemovalException
        extends RuntimeException {

    public SelfAdminRoleRemovalException() {
        super(
                "Administrator cannot remove ADMIN role from their own account"
        );
    }
}