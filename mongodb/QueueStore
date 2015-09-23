
    /**
     * This method returns a basic object that contains the key and the value given in the parameters.
     */
    protected DBObject getBasicDBObject(K key, V value) {
        return new BasicDBObject(KEY, keySerializer.serialize(key))
                .append(VALUE, valueSerializer.serialize(value));
    }

    //region Store Methods
    public void store(K key, V value) {
        BasicDBObject criteria = new BasicDBObject(KEY, keySerializer.serialize(key));
        dbCollection.update(criteria, getBasicDBObject(key, value), true, false);
    }
