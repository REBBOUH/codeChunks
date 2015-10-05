
    @Nullable
    private static InputStream executeRequest(WebTarget target, Entity entity) {
        InputStream stream = null;
        if (entity != null) {
            long start = currentTimeMillis();
            stream = getRequest(target)
                    .accept(APPLICATION_OCTET_STREAM_TYPE)
                    .post(entity, InputStream.class);
            logger.info("Request took {} s", 
                MILLISECONDS.toSeconds(currentTimeMillis() - start));
        }
        return stream;
    }
    
    
    @GET
    @Path("/{date}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendStore(@PathParam("date") final String date) {
        final LocalDate storeDate = parse(date);
        if (names == null || names.isEmpty()) {
            return noContent().entity("No elements found for this store " + date).build();
        }
        return ok((OutputStream output)-> {
            // Send request to database and stream the cursor
            // results or use reactive-streams for ex.
            output.close();
        }).build();
    }
