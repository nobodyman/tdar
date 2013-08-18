package org.tdar.core.service.workflow.workflows;

import static org.junit.Assert.*;
import static java.util.Arrays.*;

import org.junit.Test;
import org.tdar.filestore.tasks.ListArchiveTask;

public class FileArchiveWorkflowTest {

    @Test
    @SuppressWarnings("static-method")
    public void fileArchiveWorkflowExtensionsAreSupported() {
        assertTrue("A new extension has been added to the archive workflow that is not supported by TrueZip", 
                asList(ListArchiveTask.getUnderstoodExtensions()).containsAll(FileArchiveWorkflow.ARCHIVE_EXTENSIONS_SUPPORTED));
    }

}