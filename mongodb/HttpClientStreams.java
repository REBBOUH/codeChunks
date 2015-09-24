    @Nullable
    private static InputStream executeRequest(WebTarget target, Entity entity, boolean acceptGzip) {
        InputStream stream = null;
        if (entity != null) {
            long start = currentTimeMillis();
            stream = getRequest(target, acceptGzip)
                    .accept(APPLICATION_OCTET_STREAM_TYPE)
                    .post(entity, InputStream.class);
            logger.info("Request took {} s", MILLISECONDS.toSeconds(currentTimeMillis() - start));
        }
        return stream;
    }
    
    
    
