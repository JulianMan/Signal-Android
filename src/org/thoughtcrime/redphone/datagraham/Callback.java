package org.thoughtcrime.redphone.datagraham;

/**
 * Created by Julian on 3/24/2016.
 */
public interface Callback {
    abstract void doSomething();

    static Callback emptyCallback() {
        return new EmptyCallback();
    }
}

class EmptyCallback implements Callback {
    @Override
    public void doSomething() {

    }
}
