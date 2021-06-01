package com.filmbooking.booking_service.utils.permission.operation;

import java.util.Objects;

public class SimpleOperation implements Operation {

    String opName;

    public SimpleOperation(String opName) {
        this.opName = opName;
    }

    @Override
    public String name() {
        return this.opName;
    }

    @Override
    public boolean sameAs(Operation op) {
        return this.opName.equals(op.name());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.opName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SimpleOperation))
            return false;

        SimpleOperation simOp = (SimpleOperation) o;
        return Objects.equals(this.opName, simOp.opName);
    }

}
