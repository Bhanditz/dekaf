package org.jetbrains.dba.rdbms.postgre;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.dba.access.BaseErrorRecognizer;
import org.jetbrains.dba.errors.DBError;
import org.jetbrains.dba.errors.DuplicateKeyError;
import org.jetbrains.dba.errors.UnknownDBError;

import java.sql.SQLException;



/**
 * PostgreSQL specific errors recognizer.
 **/
public class PostgreErrorRecognizer extends BaseErrorRecognizer {

  @NotNull
  @Override
  protected DBError recognizeSpecificError(@NotNull final SQLException sqlException, @Nullable final String statementText) {
    int code = sqlException.getErrorCode();

    switch (code) {
      case 1:
        return new DuplicateKeyError(sqlException, statementText);
      default:
        return new UnknownDBError(sqlException, statementText);
    }
  }
}