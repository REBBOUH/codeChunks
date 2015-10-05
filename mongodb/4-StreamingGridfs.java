    public void store(String dictionaryName, final Dictionary dictionary) {
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


    public void store(String dictionaryName, final Dictionary dictionary) {
        if (dictionary == null) {
            logger.error("The given dictionary is null.");
            return;
        }
        if (isBlank(dictionaryName) || dictionary.getSize() == 0) {
            logger.error("Dictionary name is blank or dictionary is empty.");
            return;
        }
        // delete the old file.
        delete(dictionaryName);
        GridFSInputFile file = store.createFile(dictionaryName);
        try (ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(file.getOutputStream()))) {
            dictionaryToStream(dictionary, out);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return;
        }
        file.save();
    }



    public void storeFromHTTP(String requestId, final InputStream is) {
        // delete the old file.
        delete(requestId);
        GridFSInputFile file = store.createFile(is, requestId);
        file.save();
    }
