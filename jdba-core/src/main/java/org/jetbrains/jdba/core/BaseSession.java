package org.jetbrains.jdba.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jdba.intermediate.IntegralIntermediateSeance;
import org.jetbrains.jdba.intermediate.IntegralIntermediateSession;
import org.jetbrains.jdba.sql.SqlCommand;
import org.jetbrains.jdba.sql.SqlQuery;
import org.jetbrains.jdba.sql.SqlScript;
import org.jetbrains.jdba.util.Objects;

import java.util.LinkedList;
import java.util.Queue;



/**
 * @author Leonid Bushuev from JetBrains
 */
public class BaseSession implements DBSession, DBTransaction  {

  @NotNull
  private final IntegralIntermediateSession myInterSession;

  private final Queue<BaseSeanceRunner> myRunners = new LinkedList<BaseSeanceRunner>();


  protected BaseSession(@NotNull final IntegralIntermediateSession interSession) {
    myInterSession = interSession;
  }


  @Override
  public synchronized  <R> R inTransaction(final InTransaction<R> operation) {
    closeRunners();

    final R result;
    boolean ok = false;
    myInterSession.beginTransaction();
    try {
      result = operation.run(this);
      myInterSession.commit();
      ok = true;
    }
    finally {
      closeRunners();
      if (!ok) {
        myInterSession.rollback();
      }
    }

    return result;
  }


  @Override
  public synchronized void inTransaction(final InTransactionNoResult operation) {
    closeRunners();

    boolean ok = false;
    myInterSession.beginTransaction();
    try {
      operation.run(this);
      myInterSession.commit();
      ok = true;
    }
    finally {
      closeRunners();
      if (!ok) {
        myInterSession.rollback();
      }
    }
  }


  @NotNull
  @Override
  public synchronized DBCommandRunner command(@NotNull final SqlCommand command) {
    final IntegralIntermediateSeance interSeance =
            myInterSession.openSeance(command.getSourceText(), null);
    BaseCommandRunner commandRunner = new BaseCommandRunner(interSeance);
    myRunners.add(commandRunner);
    return commandRunner;
  }


  @NotNull
  @Override
  public synchronized DBCommandRunner command(@NotNull final String commandText) {
    final IntegralIntermediateSeance interSeance =
            myInterSession.openSeance(commandText, null);
    BaseCommandRunner commandRunner = new BaseCommandRunner(interSeance);
    myRunners.add(commandRunner);
    return commandRunner;
  }


  @NotNull
  @Override
  public synchronized <S> BaseQueryRunner<S> query(@NotNull final SqlQuery<S> query) {
    final IntegralIntermediateSeance interSeance =
            myInterSession.openSeance(query.getSourceText(), null);
    BaseQueryRunner<S> queryRunner = new BaseQueryRunner<S>(interSeance, query.getLayout());
    myRunners.add(queryRunner);
    return queryRunner;
  }


  @NotNull
  @Override
  public synchronized  <S> BaseQueryRunner<S> query(@NotNull final String queryText,
                                                    @NotNull final ResultLayout<S> layout) {
    SqlQuery<S> query = new SqlQuery<S>(queryText, layout);
    return this.query(query);
  }


  @NotNull
  @Override
  public synchronized DBScriptRunner script(@NotNull final SqlScript script) {
    return new BaseScriptRunner(this, script);
  }


  protected synchronized void closeRunners() {
    while (!myRunners.isEmpty()) {
      BaseSeanceRunner runner = myRunners.poll();
      runner.close();
    }
  }


  @SuppressWarnings("unchecked")
  @Nullable
  public synchronized  <I> I getSpecificService(@NotNull final Class<I> serviceClass,
                                                @NotNull final String serviceName)
      throws ClassCastException
  {
    if (serviceName.equalsIgnoreCase(Names.INTERMEDIATE_SERVICE)) {
      return Objects.castTo(serviceClass, myInterSession);
    }
    else {
      return myInterSession.getSpecificService(serviceClass, serviceName);
    }
  }


  /*
  protected synchronized void close() {
    closeRunners();
    myInterSession.close();
  }
  */

}