    public void store(String dictionaryName, final Dictionary dictionary) {
        if (dictionary == null) {
            throw new NullPointerException("The given dictionary " + dictionaryName + "is null.");
        }
        if (isBlank(dictionaryName) || dictionary.getSize() == 0) {
            logger.error("Dictionary name is blank or dictionary is empty.");
            return;
        }
        try (PipedInputStream in = new PipedInputStream()) {
            final PipedOutputStream out = new PipedOutputStream(in);
            executor.execute(() -> {
              try (ObjectOutputStream oout = new ObjectOutputStream(new GZIPOutputStream(out))) {
                dictionaryToStream(dictionary, oout);
              } catch (IOException e) {
                logger.error(e.getMessage(), e);
              }
            });
            // delete the old file (override as default behaviour).
            delete(dictionaryName);
            store.createFile(in, dictionaryName).save();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
