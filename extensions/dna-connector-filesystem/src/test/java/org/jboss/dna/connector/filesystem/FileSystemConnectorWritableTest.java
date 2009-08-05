/*
 * JBoss DNA (http://www.jboss.org/dna)
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * See the AUTHORS.txt file in the distribution for a full listing of 
 * individual contributors. 
 *
 * JBoss DNA is free software. Unless otherwise indicated, all code in JBoss DNA
 * is licensed to you under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * JBoss DNA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.dna.connector.filesystem;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.jboss.dna.common.util.FileUtil;
import org.jboss.dna.graph.DnaLexicon;
import org.jboss.dna.graph.Graph;
import org.jboss.dna.graph.JcrLexicon;
import org.jboss.dna.graph.JcrMixLexicon;
import org.jboss.dna.graph.JcrNtLexicon;
import org.jboss.dna.graph.connector.RepositorySource;
import org.jboss.dna.graph.connector.RepositorySourceException;
import org.jboss.dna.graph.connector.test.AbstractConnectorTest;
import org.jboss.dna.graph.request.InvalidRequestException;
import org.junit.Test;

public class FileSystemConnectorWritableTest extends AbstractConnectorTest {

    public static final String ARBITRARY_PROPERTIES_NOT_SUPPORTED = "This connector does not support setting arbitrary properties";

    private static final String REPO_PATH = "./src/test/resources/repositories/";
    private final String TEST_CONTENT = "Test content";

    protected File testWorkspaceRoot;
    protected File otherWorkspaceRoot;
    protected File newWorkspaceRoot;

    @Override
    protected RepositorySource setUpSource() {
        // Set the connection properties to be use the content of "./src/test/resources/repositories" as a repository ...
        String[] predefinedWorkspaceNames = new String[] {"test", "otherWorkspace", "airplanes", "cars"};
        FileSystemSource source = new FileSystemSource();
        source.setName("Test Repository");
        source.setWorkspaceRootPath(REPO_PATH);
        source.setPredefinedWorkspaceNames(predefinedWorkspaceNames);
        source.setDefaultWorkspaceName(predefinedWorkspaceNames[0]);
        source.setCreatingWorkspacesAllowed(true);
        source.setUpdatesAllowed(true);

        testWorkspaceRoot = new File(REPO_PATH, "test");
        testWorkspaceRoot.mkdir();
        
        otherWorkspaceRoot = new File(REPO_PATH, "otherWorkspace");
        otherWorkspaceRoot.mkdir();

        newWorkspaceRoot = new File(REPO_PATH, "newWorkspace");
        newWorkspaceRoot.mkdir();

        return source;
    }

    @Override
    protected void initializeContent( Graph graph ) {
        // No setup required
    }

    @Override
    public void afterEach() throws Exception {
        FileUtil.delete(testWorkspaceRoot);
        FileUtil.delete(otherWorkspaceRoot);
        FileUtil.delete(newWorkspaceRoot);
        super.afterEach();
    }

    @Test
    public void shouldBeAbleToCreateFileWithContent() {
        graph.create("/testFile").with(JcrLexicon.PRIMARY_TYPE, JcrNtLexicon.FILE).orReplace().and();
        graph.create("/testFile/jcr:content").with(JcrLexicon.PRIMARY_TYPE, DnaLexicon.RESOURCE).and(JcrLexicon.DATA,
                                                                                                     TEST_CONTENT.getBytes()).orReplace().and();

        File newFile = new File(testWorkspaceRoot, "testFile");
        assertContents(newFile, TEST_CONTENT);
    }

    @Test
    public void shouldRespectConflictBehaviorOnCreate() {
        graph.create("/testFile").with(JcrLexicon.PRIMARY_TYPE, JcrNtLexicon.FILE).orReplace().and();
        graph.create("/testFile/jcr:content").with(JcrLexicon.PRIMARY_TYPE, DnaLexicon.RESOURCE).and(JcrLexicon.DATA,
                                                                                                     TEST_CONTENT.getBytes()).orReplace().and();

        graph.create("/testFile/jcr:content").with(JcrLexicon.PRIMARY_TYPE, DnaLexicon.RESOURCE).and(JcrLexicon.DATA,
                                                                                                     "Should not overwrite".getBytes()).ifAbsent().and();

        File newFile = new File(testWorkspaceRoot, "testFile");
        assertContents(newFile, TEST_CONTENT);
    }

    @Test
    public void shouldBeAbleToCreateFileWithNoContent() {
        graph.create("/testEmptyFile").with(JcrLexicon.PRIMARY_TYPE, JcrNtLexicon.FILE).orReplace().and();

        File newFile = new File(testWorkspaceRoot, "testEmptyFile");
        assertThat(newFile.exists(), is(true));
        assertThat(newFile.isFile(), is(true));
    }

    @Test
    public void shouldBeAbleToCreateFolder() {
        graph.create("/testFolder").orReplace().and();

        File newFile = new File(testWorkspaceRoot, "testFolder");
        assertThat(newFile.exists(), is(true));
        assertThat(newFile.isDirectory(), is(true));
    }

    @Test
    public void shouldBeAbleToAddChildrenToFolder() {
        graph.create("/testFolder").orReplace().and();

        File newFolder = new File(testWorkspaceRoot, "testFolder");
        assertThat(newFolder.exists(), is(true));
        assertThat(newFolder.isDirectory(), is(true));

        graph.create("/testFolder/testFile").with(JcrLexicon.PRIMARY_TYPE, JcrNtLexicon.FILE).orReplace().and();
        graph.create("/testFolder/testfile/jcr:content").with(JcrLexicon.PRIMARY_TYPE, DnaLexicon.RESOURCE).and(JcrLexicon.DATA,
                                                                                                                TEST_CONTENT.getBytes()).orReplace().and();

        File newFile = new File(testWorkspaceRoot, "testFolder/testFile");
        assertContents(newFile, TEST_CONTENT);

    }

    @Test( expected = RepositorySourceException.class )
    public void shouldNotBeAbleToCreateInvalidTypeForRepository() {
        graph.create("/testFile").with(JcrLexicon.PRIMARY_TYPE, JcrNtLexicon.UNSTRUCTURED).orReplace().and();
    }

    @Test( expected = RepositorySourceException.class )
    public void shouldNotBeAbleToSetArbitraryProperties() {
        graph.create("/testFile").with(JcrLexicon.MIXIN_TYPES, JcrMixLexicon.LOCKABLE).orReplace().and();
    }

    @Test
    public void shouldBeAbleToCopyFile() {
        graph.create("/testFile").with(JcrLexicon.PRIMARY_TYPE, JcrNtLexicon.FILE).orReplace().and();
        graph.create("/testFile/jcr:content").with(JcrLexicon.PRIMARY_TYPE, DnaLexicon.RESOURCE).and(JcrLexicon.DATA,
                                                                                                     TEST_CONTENT.getBytes()).orReplace().and();

        File newFile = new File(testWorkspaceRoot, "testFile");
        assertContents(newFile, TEST_CONTENT);

        graph.copy("/testFile").to("/copiedFile");
        File copiedFile = new File(testWorkspaceRoot, "copiedFile");
        assertContents(copiedFile, TEST_CONTENT);
    }

    @Test
    public void shouldBeAbleToCopyFolder() {
        graph.create("/testFolder").orReplace().and();
        graph.create("/testFolder/testFile").with(JcrLexicon.PRIMARY_TYPE, JcrNtLexicon.FILE).orReplace().and();
        graph.create("/testFolder/testFile/jcr:content").with(JcrLexicon.PRIMARY_TYPE, DnaLexicon.RESOURCE).and(JcrLexicon.DATA,
                                                                                                                TEST_CONTENT.getBytes()).orReplace().and();

        File newFile = new File(testWorkspaceRoot, "testFolder/testFile");
        assertContents(newFile, TEST_CONTENT);

        graph.copy("/testFolder").to("/copiedFolder");
        File copiedFolder = new File(testWorkspaceRoot, "copiedFolder");
        assertTrue(copiedFolder.exists());
        assertTrue(copiedFolder.isDirectory());

        File copiedFile = new File(testWorkspaceRoot, "copiedFolder/testFile");
        assertContents(copiedFile, TEST_CONTENT);
    }

    @Test
    public void shouldBeAbleToMoveFile() {
        graph.create("/testFile").with(JcrLexicon.PRIMARY_TYPE, JcrNtLexicon.FILE).orReplace().and();
        graph.create("/testFile/jcr:content").with(JcrLexicon.PRIMARY_TYPE, DnaLexicon.RESOURCE).and(JcrLexicon.DATA,
                                                                                                     TEST_CONTENT.getBytes()).orReplace().and();

        File newFile = new File(testWorkspaceRoot, "testFile");
        assertContents(newFile, TEST_CONTENT);

        graph.create("/newFolder").orReplace().and();

        graph.move("/testFile").into("/newFolder");
        assertThat(newFile.exists(), is(false));

        File copiedFile = new File(testWorkspaceRoot, "newFolder/testFile");
        assertContents(copiedFile, TEST_CONTENT);
    }

    @Test
    public void shouldBeAbleToMoveFolder() {
        graph.create("/testFolder").orReplace().and();
        graph.create("/testFolder/testFile").with(JcrLexicon.PRIMARY_TYPE, JcrNtLexicon.FILE).orReplace().and();
        graph.create("/testFolder/testFile/jcr:content").with(JcrLexicon.PRIMARY_TYPE, DnaLexicon.RESOURCE).and(JcrLexicon.DATA,
                                                                                                                TEST_CONTENT.getBytes()).orReplace().and();

        File newFile = new File(testWorkspaceRoot, "testFolder/testFile");
        assertContents(newFile, TEST_CONTENT);

        graph.create("/newFolder").orReplace().and();

        graph.move("/testFolder").into("/newFolder");
        assertThat(newFile.exists(), is(false));

        File copiedFolder = new File(testWorkspaceRoot, "newFolder/testFolder");
        assertTrue(copiedFolder.exists());
        assertTrue(copiedFolder.isDirectory());

        File copiedFile = new File(testWorkspaceRoot, "newFolder/testFolder/testFile");
        assertContents(copiedFile, TEST_CONTENT);
    }

    @Test
    public void shouldBeAbleToDeleteFolderWithContents() {
        graph.create("/testFolder").orReplace().and();
        graph.create("/testFolder/testFile").with(JcrLexicon.PRIMARY_TYPE, JcrNtLexicon.FILE).orReplace().and();
        graph.create("/testFolder/testFile/jcr:content").with(JcrLexicon.PRIMARY_TYPE, DnaLexicon.RESOURCE).and(JcrLexicon.DATA,
                                                                                                                TEST_CONTENT.getBytes()).orReplace().and();

        File newFolder = new File(testWorkspaceRoot, "testFolder");
        assertTrue(newFolder.exists());
        assertTrue(newFolder.isDirectory());

        File newFile = new File(testWorkspaceRoot, "testFolder/testFile");
        assertContents(newFile, TEST_CONTENT);

        graph.delete("/testFolder");

        assertThat(newFolder.exists(), is(false));
    }

    @Test
    public void shouldBeAbleToDeleteFile() {
        graph.create("/testFolder").orReplace().and();
        graph.create("/testFolder/testFile").with(JcrLexicon.PRIMARY_TYPE, JcrNtLexicon.FILE).orReplace().and();
        graph.create("/testFolder/testFile/jcr:content").with(JcrLexicon.PRIMARY_TYPE, DnaLexicon.RESOURCE).and(JcrLexicon.DATA,
                                                                                                                TEST_CONTENT.getBytes()).orReplace().and();

        File newFolder = new File(testWorkspaceRoot, "testFolder");
        assertTrue(newFolder.exists());
        assertTrue(newFolder.isDirectory());

        File newFile = new File(testWorkspaceRoot, "testFolder/testFile");
        assertContents(newFile, TEST_CONTENT);

        graph.delete("/testFolder/testFile");

        assertTrue(newFolder.exists());
        assertThat(newFile.exists(), is(false));
    }

    /**
     * Since the FS connector does not support UUIDs (under the root node), all clones are just copies (clone for
     * non-referenceable nodes is a copy to the corresponding path).
     */
    @Test
    public void shouldBeAbleToCloneFolder() {
        graph.useWorkspace("otherWorkspace");
        graph.create("/testFolder").orReplace().and();
        graph.create("/testFolder/testFile").with(JcrLexicon.PRIMARY_TYPE, JcrNtLexicon.FILE).orReplace().and();
        graph.create("/testFolder/testFile/jcr:content").with(JcrLexicon.PRIMARY_TYPE, DnaLexicon.RESOURCE).and(JcrLexicon.DATA,
                                                                                                                TEST_CONTENT.getBytes()).orReplace().and();

        File newFile = new File(otherWorkspaceRoot, "testFolder/testFile");
        assertContents(newFile, TEST_CONTENT);

        graph.useWorkspace("test");
        graph.clone("/testFolder").fromWorkspace("otherWorkspace").as("clonedFolder").into("/").failingIfAnyUuidsMatch();
        File copiedFolder = new File(testWorkspaceRoot, "clonedFolder");
        assertTrue(copiedFolder.exists());
        assertTrue(copiedFolder.isDirectory());

        File copiedFile = new File(testWorkspaceRoot, "clonedFolder/testFile");
        assertContents(copiedFile, TEST_CONTENT);
    }

    /**
     * Since the FS connector does not support UUIDs (under the root node), all clones are just copies (clone for
     * non-referenceable nodes is a copy to the corresponding path).
     */
    @Test
    public void shouldBeAbleToCloneFile() {
        graph.useWorkspace("otherWorkspace");
        graph.create("/testFile").with(JcrLexicon.PRIMARY_TYPE, JcrNtLexicon.FILE).orReplace().and();
        graph.create("/testFile/jcr:content").with(JcrLexicon.PRIMARY_TYPE, DnaLexicon.RESOURCE).and(JcrLexicon.DATA,
                                                                                                     TEST_CONTENT.getBytes()).orReplace().and();

        File newFile = new File(otherWorkspaceRoot, "testFile");
        assertContents(newFile, TEST_CONTENT);

        graph.useWorkspace("test");
        graph.clone("/testFile").fromWorkspace("otherWorkspace").as("clonedFile").into("/").failingIfAnyUuidsMatch();
        File copiedFile = new File(testWorkspaceRoot, "clonedFile");
        assertContents(copiedFile, TEST_CONTENT);
    }

    @Test( expected = InvalidRequestException.class )
    public void shouldNotBeAbleToReorderFolder() {
        graph.create("/testFolder").orReplace().and();
        graph.create("/testFolder2").orReplace().and();

        File newFolder = new File(testWorkspaceRoot, "testFolder");
        assertTrue(newFolder.exists());
        assertTrue(newFolder.isDirectory());

        File newFolder2 = new File(testWorkspaceRoot, "testFolder2");
        assertTrue(newFolder2.exists());
        assertTrue(newFolder2.isDirectory());

        graph.move("/testFolder2").before("/testFolder");
    }

    @Test( expected = InvalidRequestException.class )
    public void shouldNotBeAbleToReorderFile() {
        graph.create("/testFolder").orReplace().and();
        graph.create("/testFolder/testFile").with(JcrLexicon.PRIMARY_TYPE, JcrNtLexicon.FILE).orReplace().and();
        graph.create("/testFolder/testFile/jcr:content").with(JcrLexicon.PRIMARY_TYPE, DnaLexicon.RESOURCE).and(JcrLexicon.DATA,
                                                                                                                TEST_CONTENT.getBytes()).orReplace().and();
        graph.create("/testFolder/testFile2").with(JcrLexicon.PRIMARY_TYPE, JcrNtLexicon.FILE).orReplace().and();
        graph.create("/testFolder/testFile2/jcr:content").with(JcrLexicon.PRIMARY_TYPE, DnaLexicon.RESOURCE).and(JcrLexicon.DATA,
                                                                                                                 TEST_CONTENT.getBytes()).orReplace().and();

        File newFolder = new File(testWorkspaceRoot, "testFolder");
        assertTrue(newFolder.exists());
        assertTrue(newFolder.isDirectory());

        File newFile = new File(testWorkspaceRoot, "testFolder/testFile");
        assertContents(newFile, TEST_CONTENT);
        File newFile2 = new File(testWorkspaceRoot, "testFolder/testFile2");
        assertContents(newFile2, TEST_CONTENT);
        
        graph.move("/testFolder/testFile2").before("/testFolder/testFile");
    }

    @Test
    public void shouldBeAbleToRenameFolder() {
        graph.create("/testFolder").orReplace().and();
        graph.create("/testFolder/testFile").with(JcrLexicon.PRIMARY_TYPE, JcrNtLexicon.FILE).orReplace().and();
        graph.create("/testFolder/testFile/jcr:content").with(JcrLexicon.PRIMARY_TYPE, DnaLexicon.RESOURCE).and(JcrLexicon.DATA,
                                                                                                                TEST_CONTENT.getBytes()).orReplace().and();

        File newFile = new File(testWorkspaceRoot, "testFolder/testFile");
        assertContents(newFile, TEST_CONTENT);

        graph.move("/testFolder").as("newFolder").into("/");
        assertThat(newFile.exists(), is(false));

        File copiedFolder = new File(testWorkspaceRoot, "newFolder");
        assertTrue(copiedFolder.exists());
        assertTrue(copiedFolder.isDirectory());

        File copiedFile = new File(testWorkspaceRoot, "newFolder/testFile");
        assertContents(copiedFile, TEST_CONTENT);
    }

    @Test
    public void shouldBeAbleToRenameFile() {
        graph.create("/testFile").with(JcrLexicon.PRIMARY_TYPE, JcrNtLexicon.FILE).orReplace().and();
        graph.create("/testFile/jcr:content").with(JcrLexicon.PRIMARY_TYPE, DnaLexicon.RESOURCE).and(JcrLexicon.DATA,
                                                                                                     TEST_CONTENT.getBytes()).orReplace().and();

        File newFile = new File(testWorkspaceRoot, "testFile");
        assertContents(newFile, TEST_CONTENT);

        graph.move("/testFile").as("copiedFile").into("/");
        assertThat(newFile.exists(), is(false));

        File copiedFile = new File(testWorkspaceRoot, "copiedFile");
        assertContents(copiedFile, TEST_CONTENT);
    }

    @Test
    public void shouldBeAbleToCreateWorkspace() {
        graph.createWorkspace().named("newWorkspace");

        graph.useWorkspace("newWorkspace");

        graph.create("/testFile").with(JcrLexicon.PRIMARY_TYPE, JcrNtLexicon.FILE).orReplace().and();
        graph.create("/testFile/jcr:content").with(JcrLexicon.PRIMARY_TYPE, DnaLexicon.RESOURCE).and(JcrLexicon.DATA,
                                                                                                     TEST_CONTENT.getBytes()).orReplace().and();

        File newFile = new File(newWorkspaceRoot, "testFile");
        assertContents(newFile, TEST_CONTENT);
    }

    @Test
    public void shouldBeAbleToCloneWorkspace() {
        graph.create("/testFile").with(JcrLexicon.PRIMARY_TYPE, JcrNtLexicon.FILE).orReplace().and();
        graph.create("/testFile/jcr:content").with(JcrLexicon.PRIMARY_TYPE, DnaLexicon.RESOURCE).and(JcrLexicon.DATA,
                                                                                                     TEST_CONTENT.getBytes()).orReplace().and();

        File newFile = new File(testWorkspaceRoot, "testFile");
        assertContents(newFile, TEST_CONTENT);

        graph.createWorkspace().clonedFrom("test").named("newWorkspace");

        newFile = new File(newWorkspaceRoot, "testFile");
        assertContents(newFile, TEST_CONTENT);
        
    }

    @Test
    public void shouldBeAbleToCreateDeepPath() {
        String pathName = "";

        for (int i = 0; i < 30; i++) {
            pathName += "/test";
            graph.create(pathName).orReplace().and();
        }
    }

    @Test( expected = RepositorySourceException.class )
    public void shouldNotBeAbleToCreateTooDeepPath() {
        String pathName = "";

        for (int i = 0; i < 100; i++) {
            pathName += "/testFolder";
            graph.create(pathName).orReplace().and();
        }
    }

    protected void assertContents( File file,
                                   String contents ) {
        assertTrue(file.exists());
        assertTrue(file.isFile());

        StringBuilder buff = new StringBuilder();
        final int BUFF_SIZE = 8192;
        byte[] bytes = new byte[BUFF_SIZE];
        int len;
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);

            while (-1 != (len = fis.read(bytes, 0, BUFF_SIZE))) {
                buff.append(new String(bytes, 0, len));
            }

            assertThat(buff.toString(), is(contents));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail(ioe.getMessage());
            return;
        } finally {
            try {
                fis.close();
            } catch (Exception ignore) {
            }
        }
    }
}
