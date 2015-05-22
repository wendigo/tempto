/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.convention

import com.teradata.test.internal.convention.AnnotatedFileParser.SectionParsingResult
import spock.lang.Specification

import static com.google.common.collect.Iterables.getOnlyElement
import static com.teradata.test.fulfillment.table.MutableTableRequirement.State.CREATED
import static com.teradata.test.fulfillment.table.MutableTableRequirement.State.LOADED
import static org.apache.commons.io.IOUtils.toInputStream

class SqlQueryDescriptorTest
        extends Specification
{
  def 'parses mutable table properties'()
  {
    setup:
    String fileContent = '-- mutable_tables: table1|loaded|table1_name, table2|created, table3'
    SectionParsingResult parsingResult = parseSection(fileContent)
    SqlQueryDescriptor queryDescriptor = new SqlQueryDescriptor(parsingResult)

    expect:
    queryDescriptor.mutableTableDescriptors.size() == 3

    queryDescriptor.mutableTableDescriptors[0].tableDefinitionName == 'table1'
    queryDescriptor.mutableTableDescriptors[0].state == LOADED;
    queryDescriptor.mutableTableDescriptors[0].name == 'table1_name'

    queryDescriptor.mutableTableDescriptors[1].tableDefinitionName == 'table2'
    queryDescriptor.mutableTableDescriptors[1].state == CREATED;
    queryDescriptor.mutableTableDescriptors[1].name == 'table2'

    queryDescriptor.mutableTableDescriptors[2].tableDefinitionName == 'table3'
    queryDescriptor.mutableTableDescriptors[2].state == LOADED;
    queryDescriptor.mutableTableDescriptors[2].name == 'table3'
  }

  def 'should fail duplicate mutable table name'()
  {
    setup:
    String fileContent = '-- mutable_tables: table1, table1'
    SectionParsingResult parsingResult = parseSection(fileContent)
    SqlQueryDescriptor queryDescriptor = new SqlQueryDescriptor(parsingResult)

    when:
    queryDescriptor.mutableTableDescriptors

    then:
    def e = thrown(IllegalStateException)
    e.message == 'Table with name table1 is defined twice'
  }

  def parseSection(String content)
  {
    getOnlyElement(new AnnotatedFileParser().parseFile(toInputStream(content)))
  }
}