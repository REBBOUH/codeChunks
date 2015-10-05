
public class JobQueueStore implements QueueStore<Job> {
    @Override public void store(Long id, Job value) {
        dbCollection.update(getCriteria(id), getBasicDBObject(key, value), true, false);
    }
    @Override public void delete(Long key) {
        // delete the job from the collection using it's id.
    }
    // ...
