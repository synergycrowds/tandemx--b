package tandemx.db;

import tandemx.model.Execution;
import tandemx.model.ExecutionCurrencyPair;
import tandemx.model.ExecutionDescription;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DBAExecutionsHib implements DBAExecutions {
    private EntityManagerFactory factory;

    public DBAExecutionsHib(String dbName) {
        this.factory = Persistence.createEntityManagerFactory(dbName);
    }

    public void close() {
        factory.close();
    }

    @Override
    public ExecutionDescription getOldestUncompletedExecutionDescription() {
        final EntityManager manager = factory.createEntityManager();
        manager.getTransaction().begin();

        List<Execution> executions = (List<Execution>) manager
                .createQuery("select e from Execution e where e.executionTimestampEnd is null order by e.id")
                .setMaxResults(1)
                .getResultList();

        Execution execution;
        if (executions.size() <= 0) {
            return null;
        } else {
            execution = executions.get(0);
        }

        List<ExecutionCurrencyPair> executionCurrencyPairs = (List<ExecutionCurrencyPair>) manager
                .createQuery("select ecp from ExecutionCurrencyPair ecp where ecp.executionId = :exeId")
                .setParameter("exeId", execution.getId())
                .getResultList();

        manager.getTransaction().commit();
        manager.close();
        return new ExecutionDescription(execution, executionCurrencyPairs);
    }

    @Override
    public void updateExecutionCompletionTimestamps(Integer executionId, LocalDateTime executionTimestampBegin, LocalDateTime executionTimestampEnd) {
        final EntityManager manager = factory.createEntityManager();
        manager.getTransaction().begin();
        Execution execution = manager.find(Execution.class, executionId);
        // TODO: 2/25/2019 throw exception if execution is null
        execution.setExecutionTimestampBegin(executionTimestampBegin);
        execution.setExecutionTimestampEnd(executionTimestampEnd);
        manager.getTransaction().commit();
        manager.close();
    }

    @Override
    public Execution getLatestExecution() {
        final EntityManager manager = factory.createEntityManager();
        manager.getTransaction().begin();

        List<Execution> executions = (List<Execution>) manager
                .createQuery("select e from Execution e order by e.dataTimestampEnd desc")
                .setMaxResults(1)
                .getResultList();

        Execution result;
        if (executions.size() <= 0) {
            result = null;
        } else {
            result = executions.get(0);
        }

        manager.getTransaction().commit();
        manager.close();
        return result;
    }

    @Override
    public ExecutionDescription insertExecutionDescription(ExecutionDescription executionDescription) {
        final EntityManager manager = factory.createEntityManager();
        manager.getTransaction().begin();
        Execution execution = executionDescription.getExecution();
        manager.persist(execution);
        executionDescription.setExecutionId(execution.getId());
        executionDescription.getExecutionCurrencyPairs().forEach(ecp -> manager.persist(ecp));
        manager.getTransaction().commit();
        manager.close();
        return executionDescription;
    }

    @Override
    public List<ExecutionDescription> getExecutionDescriptionsByDataTimestampEnd(LocalDate dataTimestampEnd) {
        final EntityManager manager = factory.createEntityManager();
        manager.getTransaction().begin();

        List<Execution> executions = (List<Execution>) manager
                .createQuery("select e from Execution e where e.dataTimestampEnd = :tmstpEnd")
                .setParameter("tmstpEnd", dataTimestampEnd)
                .getResultList();

        List<ExecutionDescription> executionDescriptions = new ArrayList<>();

        for (Execution execution: executions) {
            List<ExecutionCurrencyPair> executionCurrencyPairs = (List<ExecutionCurrencyPair>) manager
                    .createQuery("select ecp from ExecutionCurrencyPair ecp where ecp.executionId = :exeId")
                    .setParameter("exeId", execution.getId())
                    .getResultList();
            executionDescriptions.add(new ExecutionDescription(execution, executionCurrencyPairs));
        }

        manager.getTransaction().commit();
        manager.close();
        return executionDescriptions;
    }

    @Override
    public List<ExecutionCurrencyPair> insertExecutionCurrencyPairs(List<ExecutionCurrencyPair> executionCurrencyPairs) {
        final EntityManager manager = factory.createEntityManager();
        manager.getTransaction().begin();
        executionCurrencyPairs.forEach(ecp -> manager.persist(ecp));
        manager.getTransaction().commit();
        manager.close();
        return executionCurrencyPairs;
    }

    @Override
    public ExecutionDescription getExecutionDescriptionById(int executionId) {
        final EntityManager manager = factory.createEntityManager();
        manager.getTransaction().begin();

        List<Execution> executions = (List<Execution>) manager
                .createQuery("select e from Execution e where e.id = :eId")
                .setParameter("eId", executionId)
                .setMaxResults(1)
                .getResultList();

        Execution execution;
        if (executions.size() <= 0) {
            return null;
        } else {
            execution = executions.get(0);
        }

        List<ExecutionCurrencyPair> executionCurrencyPairs = (List<ExecutionCurrencyPair>) manager
                .createQuery("select ecp from ExecutionCurrencyPair ecp where ecp.executionId = :exeId")
                .setParameter("exeId", execution.getId())
                .getResultList();

        manager.getTransaction().commit();
        manager.close();
        return new ExecutionDescription(execution, executionCurrencyPairs);
    }

    @Override
    public Integer getOldestUncompletedExecutionId() {
        final EntityManager manager = factory.createEntityManager();
        manager.getTransaction().begin();

        List<Execution> executions = (List<Execution>) manager
                .createQuery("select e from Execution e where e.executionTimestampEnd is null order by e.id")
                .setMaxResults(1)
                .getResultList();

        Integer executionId;
        if (executions.size() <= 0) {
            executionId = null;
        } else {
            executionId = executions.get(0).getId();
        }

        manager.getTransaction().commit();
        manager.close();
        return executionId;
    }
}
