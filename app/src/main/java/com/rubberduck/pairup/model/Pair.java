package com.rubberduck.pairup.model;

public class Pair {
    private Shirt shirt;
    private Trouser trouser;

    public Pair(Shirt shirt, Trouser trouser) {
        this.shirt = shirt;
        this.trouser = trouser;
    }

    public Shirt getShirt() {
        return shirt;
    }

    public Trouser getTrouser() {
        return trouser;
    }
}
