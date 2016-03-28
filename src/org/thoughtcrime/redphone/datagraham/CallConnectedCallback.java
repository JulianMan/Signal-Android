package org.thoughtcrime.redphone.datagraham;

public interface CallConnectedCallback {
    abstract void doSomething(String message);
}

class EmptyCallConnectedCallback implements CallConnectedCallback {
    @Override
    public void doSomething(String message) {

    }
}