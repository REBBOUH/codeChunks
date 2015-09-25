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
    
    
    @GET
    @Path("/{date}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendStore(
            @PathParam("date") final String date,
            @QueryParam("name") final List<String> names
    ) {
        final LocalDate storeDate;
        try {
            storeDate = parse(date);
        } catch (IllegalArgumentException e) {
            return ServerApiUtils.buildErrorResponse("Invalid date " + date + " please use this format yyyy-MM-dd Ex : 2015-01-22.");
        }
        if (names == null || names.isEmpty()) {
            return noContent().entity("No elements found for this store " + date).build();
        }
        return ok(new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                // Send request to database and stream the cursor results or use reactive-streams for ex.
                output.close();
            }
        }).build();
    }
