package com.sysgears.user.subscription;

import com.sysgears.user.model.User;
import lombok.Data;
import lombok.Getter;

@Data
public class UserUpdatedEvent {
    private final Mutation mutation;
    private final User user;

    public enum Mutation {
        ADD_USER("addUser"),
        EDIT_USER("editUser"),
        DELETE_USER("deleteUser");

        @Getter
        private final String operation;

        Mutation(String operation) {
            this.operation = operation;
        }
    }
}
