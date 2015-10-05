
    @Override
    public void run() {
        String queueName = getQueueName(this.getClass());
        Queue<T> jobQueue = getQueue(queueName);
        if (jobQueue == null) {
            throw new IllegalStateException("Unable to create queue on store.");
        }
        while (!interrupted()) {
            T job = null;
            try {
                job = jobQueue.poll();
                if (job != null) {
                    job.setProcessor(getRunningHost() + COLON + getThreadName());
                    setJob(job);
                    job.startJob();
                    setState(RUNNING);
                    syncWithDistributedStore();
                    proceed();
                } else {
                    setJob(null);
                    setState(WAITING);
                    syncWithDistributedStore();
                    sleep(SECONDS.toMillis(1));
                }
            } catch (Exception e) {
                if (job != null) {
                    // if We interrupted the worker we enqueue the job back.
                    getJob().setType(STANDARD);
                    enqueueJob(this.getClass(), getJob());
                }
                logger.error(e.getMessage(), e);
                break;
            }
        }
    }

    public void syncWithDistributedStore() {
        try {
            getStartedWorkers().lock(id);
            BamosWorker storedWorker = getStartedWorkers().get(id);
            if (storedWorker == null || storedWorker.state != STOPPED) {
                getStartedWorkers().put(id, this, 3, getState() == WAITING ? SECONDS : HOURS);
            }
        } finally {
            getStartedWorkers().unlock(id);
        }
    }

    public synchronized void start() {
        if (state == NEW || (state == STOPPED && future != null)) {
            workers.put(this.id, this);
            future = getExecutor().submit(this);
        }
    }

    public synchronized void stop() {
        state = STOPPED;
        if (future != null) {
            logger.info("Stopped Worker {}.", threadName);
            future.cancel(true);
        }
    }

    private void proceed() {
        try {
            preWork();
            work();
            postWork();
            getJob().endJob();
            archiveJob();
            logger.info("Processing {} took {} s",
                    getJob().getClass().getSimpleName(),
                    MILLISECONDS.toSeconds(getJob().getTotalDuration()));
        } catch (Exception e) {
            getJob().endJob();
            getJob().setFailureTime(currentTimeMillis());
            getJob().setExceptionMessage(e.toString());
            logger.error(e.getMessage(), e);
            int retryCount = getJob().getRetryCount();
            if (retryCount < 3) {
                logger.info("Retrying Job {}.", getJob().toString());
                getJob().setType(RETRY);
                getJob().setFuture(now().plusMinutes(retryCount * 10 + 1).getMillis());
                getJob().setRetryCount(retryCount + 1);
                enqueueJob(this.getClass(), getJob());
            } else {
                IMap<UUID, BamosJob> failedJobs = getFailedJobs();
                if (failedJobs != null) {
                    failedJobs.put(getJob().getId(), getJob(), 2, DAYS);
                } else {
                    logger.error("The failed jobs list can't be retrieved.");
                }
            }
        }
    }
