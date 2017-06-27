package org.jetbrains.dekaf.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.dekaf.text.Scriptum;



/**
 * @author Leonid Bushuev from JetBrains
 **/
public class PostgresTestHelper extends BaseTestHelper<DBFacade> {

  public PostgresTestHelper(@NotNull final DBFacade db) {
    super(db, Scriptum.of(PostgresTestHelper.class));
    getSchemasNotToZap().add("pg_catalog");
  }


  @Override
  public void prepareX1() {
    performCommand("create or replace view X1 as select 1");
  }

  @Override
  public void prepareX1000() {
    performCommand(getScriptum(), "X1000");
  }

  @Override
  public void prepareX1000000() {
    performCommand(getScriptum(), "X1000000");
  }

  @Override
  protected void zapSchemaInternally(final ConnectionInfo connectionInfo) {
    ConnectionInfo info = getDb().getConnectionInfo();
    if (info.serverVersion.isOrGreater(9, 1)) performMetaQueryCommands(getScriptum(), "ZapExtensionsMetaQuery");
    performMetaQueryCommands(getScriptum(), "ZapSchemaMetaQuery");
  }
}
