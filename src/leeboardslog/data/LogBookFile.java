/*
 * Copyright 2018 Albert Santos.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leeboardslog.data;

import com.leeboardtools.util.ChangeId;
import com.leeboardtools.util.ResourceSource;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * The file underlying a log book is kept separate from the log book. This class
 * manages that underlying file.
 * @author Albert Santos
 */
public class LogBookFile {
    private static final Logger LOG = Logger.getLogger(LogBookFile.class.getName());

    public static final String FILE_TAG = "LeeboardsLog:";
    public static final String VERSION_VALUE = "1.0.0.0";
    
    public static final String CURRENT_LOGBOOK_KEY = "currentLogBook";
    
    public static final String PREF_IS_COMPRESS_FILE = "isCompressFile";
    
    LogBook logBook = new LogBook();
    final ChangeId.Tracker changeIdTracker = logBook.newChangeIdTracker();

    File file;
    
    boolean isLogBookChanged = true;
    Header header;
    
    Optional<Boolean> optIsCompressFile = Optional.empty();
    
    JSONObject updatedJSONObject;
    JSONObject lastJSONObject;
    
    
    /**
     * Determines if the file represented by a file name is potentially a log book file.
     * @param file  The file object identifying the file.
     * @return <code>true</code> if the file can potentially be a log book file.
     */
    public static boolean isPossibleFileLogBook(File file) {
        if (!file.exists()) {
            return false;
        }
        if (!file.isFile()) {
            return false;
        }
        
        try {
            InputStream inputStream = fileToInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            Header header = readHeader(file.getName(), reader);
            if (!isVersionSupported(header.version)) {
                return false;
            }
        } catch (FileNotFoundException | JSONException | FileException ex) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Determines if a version string is supported.
     * @param version
     * @return <code>true</code> if the version is supported.
     */
    public static boolean isVersionSupported(String version) {
        return VERSION_VALUE.equals(version);
    }
    
    
    /**
     * The exception thrown by this class on file errors.
     */
    public static class FileException extends java.lang.Exception {

        private FileException(String message) {
            super(message);
        }
        
    }
    
    
    public static class Header {
        public String version;
        public long lastUpdateId;
        public String lastUpdateAuthor;
        public Instant lastUpdateInstant;
    }
    
    public static Header readHeader(String fileName, BufferedReader reader) throws FileException {
        try {
            String fileTag = reader.readLine();
            if (!FILE_TAG.equals(fileTag)) {
                String message = ResourceSource.getString("Error.logBookFileHeaderTagInvalid", fileName);
                throw new FileException(message);
            }
            
            Header header = new Header();
            header.version = reader.readLine();
            
            String lastUpdateIdText = reader.readLine();
            header.lastUpdateId = Long.parseLong(lastUpdateIdText);
            
            header.lastUpdateAuthor = reader.readLine();
            
            String lastUpdateInstantText = reader.readLine();
            header.lastUpdateInstant = Instant.parse(lastUpdateInstantText);

            return header;
        } catch (IOException | NumberFormatException | DateTimeParseException ex) {
            LOG.log(Level.SEVERE, null, ex);
            String message = ResourceSource.getString("Error.logBookFileHeaderReadFailed", fileName, ex.getLocalizedMessage());
            throw new FileException(message);
        }
    }
    
    public static void writeHeader(Header header, String fileName, BufferedWriter writer) throws FileException {
        try {
            writer.write(FILE_TAG);
            writer.newLine();
            
            writer.write(header.version);
            writer.newLine();
            
            writer.write(Long.toString(header.lastUpdateId));
            writer.newLine();
            
            writer.write(header.lastUpdateAuthor);
            writer.newLine();
            
            writer.write(header.lastUpdateInstant.toString());
            writer.newLine();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            String message = ResourceSource.getString("Error.logBookFileHeaderWriteFailed", fileName, ex.getLocalizedMessage());
            throw new FileException(message);
        }
    }
    
    /**
     * Creates a new log book file. If the file already exists, it is cleared.
     * @param file  The file object identifying the file.
     * @return  The log book.
     * @throws leeboardslog.data.LogBookFile.FileException 
     */
    public static LogBookFile createLogBookFile(File file) throws FileException {
        String fileName = file.getName();
        try {
            // Make sure we can write to the file...
            file.createNewFile();
            Header header = new Header();
            header.version = VERSION_VALUE;
            
            return new LogBookFile(file, header);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            String message = ResourceSource.getString("Error.createLogBookFileFailed", fileName, ex.getLocalizedMessage());
            throw new FileException(message);
        }
    }
    
    /**
     * Opens an existing log book file.
     * @param file  The file object identifying the file.
     * @return  The log book file.
     * @throws leeboardslog.data.LogBookFile.FileException 
     */
    public static LogBookFile openLogBookFile(File file) throws FileException {
        String fileName = file.getName();
        InputStream inputStream = null;
        try {
            inputStream = fileToInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            
            Header header = readHeader(fileName, reader);
            if (!isVersionSupported(header.version)) {
                LOG.log(Level.SEVERE, null, "Unsupported version: " + header.version);
                String message = ResourceSource.getString("Error.openLogBookUnsupportedVersion", fileName, header.version, ResourceSource.getAppName());
                throw new FileException(message);
            }
            
            JSONObject rootJSONObject = new JSONObject(new JSONTokener(reader));

            JSONObject logBookJSONObject = rootJSONObject.optJSONObject(CURRENT_LOGBOOK_KEY);
            if (logBookJSONObject == null) {
                LOG.log(Level.SEVERE, null, "Missing " + CURRENT_LOGBOOK_KEY);
                String message = ResourceSource.getString("Error.openLogBookMissingKey", fileName, CURRENT_LOGBOOK_KEY);
                throw new FileException(message);
            }
            
            LogBookFile logBookFile = new LogBookFile(file, header);
            logBookFile.logBook = LogBook.fromJSON(logBookJSONObject);
            return logBookFile;
            
        } catch (FileNotFoundException ex) {
            LOG.log(Level.SEVERE, null, ex);
            String message = ResourceSource.getString("Error.openLogBookFileNotFound", fileName, ex.getLocalizedMessage());
            throw new FileException(message);
        } catch (JSONException ex) {
            LOG.log(Level.SEVERE, null, ex);
            String message = ResourceSource.getString("Error.openLogBookJSONInvalid", fileName, ex.getLocalizedMessage());
            throw new FileException(message);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    protected LogBookFile(File file, Header header) {
        this.file = file;
        this.header = header;
    }
    
    
    /**
     * @return The name of the underlying file.
     */
    public final String getFileName() {
        return this.file.getPath();
    }
    
    
    /**
     * @return The log book the file represents.
     */
    public final LogBook getLogBook() {
        return this.logBook;
    }
    
    
    /**
     * @return The optional value that overrides the user preference for compressing
     * the file.
     */
    public final Optional<Boolean> getOptIsCompressFile() {
        return this.optIsCompressFile;
    }
    
    /**
     * Sets the optional value that can override the user preference for compressing
     * the file. The compression state is only changed when the file is actually
     * updated.
     * @param optIsCompressFile The new optional value, setting it to the result of {@link Optional#empty() }
     * results in the user preference being used.
     */
    public void setOptIsCompressFile(Optional<Boolean> optIsCompressFile) {
        this.optIsCompressFile = optIsCompressFile;
    }
    
    
    /**
     * Determines if there have been any modifications to the log book since the last
     * time the file was updated.
     * @return <code>true</code> if the log book's contents have changed.
     */
    public boolean isLogBookChanged() {
        if (this.changeIdTracker.isChangedWithClean()) {
            updateIsLogBookChanged();
        }
        return this.isLogBookChanged;
    }
    
    protected void updateIsLogBookChanged() {
        // If there's no previous JSON text, then we're always changed...
        if (this.lastJSONObject == null) {
            this.isLogBookChanged = true;
        }
        else {
            // We'll compare the current JSON text with the previous JSON text.
            this.updatedJSONObject = this.logBook.toJSONObject();
            this.isLogBookChanged = !this.updatedJSONObject.similar(this.lastJSONObject);
        }
    }

    /**
     * Saves the log book to its file if the log book has changed.
     * @throws leeboardslog.data.LogBookFile.FileException 
     */
    public void updateFile() throws FileException {
        if (!isLogBookChanged()) {
            // Nothing changed...
            return;
        }

        OutputStream stream = null;
        BufferedWriter writer = null;
        String fileName = this.file.getPath();
        try {
            stream = getOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(stream));
            
            ++this.header.lastUpdateId;
            this.header.lastUpdateInstant = Instant.now();
            this.header.lastUpdateAuthor = this.logBook.getActiveAuthor();
            
            writeHeader(this.header, fileName, writer);
            
            JSONObject rootJSONObject = new JSONObject();
            
            if (this.updatedJSONObject == null) {
                this.updatedJSONObject = this.logBook.toJSONObject();
            }
            
            rootJSONObject.put(CURRENT_LOGBOOK_KEY, this.updatedJSONObject);
            
            // TODO: Need to add the diff...
            // We need a diff list.
            
            if (shouldCompressFile()) {
                writer.write(rootJSONObject.toString());
            }
            else {
                writer.write(rootJSONObject.toString(4));
            }
            
            this.lastJSONObject = this.updatedJSONObject;
            this.isLogBookChanged = false;
            
        } catch (IOException ex) {
            
            LOG.log(Level.SEVERE, null, ex);
            String message = ResourceSource.getString("Error.logBookUpdateFileFailed", fileName, ex.getLocalizedMessage());
            throw new FileException(message);
            
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
                String message = ResourceSource.getString("Error.logBookUpdateFileFailed", fileName, ex.getLocalizedMessage());
                throw new FileException(message);
            }
        }
    }
    
    
    protected final boolean shouldCompressFile() {
        final boolean isCompressFile = this.optIsCompressFile.orElseGet(() -> {
            Preferences prefs = Preferences.userNodeForPackage(getClass());
            return prefs.getBoolean(PREF_IS_COMPRESS_FILE, true);
        });
        return isCompressFile;
    }
    
    
    protected OutputStream getOutputStream() throws FileNotFoundException, IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(this.file);
        
        if (shouldCompressFile()) {
            GZIPOutputStream compressedOutputStream = new GZIPOutputStream(fileOutputStream);
            return compressedOutputStream;
        }
        
        return fileOutputStream;
    }
    
    
    protected static InputStream fileToInputStream(File file) throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            GZIPInputStream compressedInputStream = new GZIPInputStream(fileInputStream);
            return compressedInputStream;
        } catch (IOException ex) {
            // Presume the file is not a GZIP file...
            try {
                fileInputStream.close();
            } catch (IOException ex1) {
                LOG.log(Level.SEVERE, null, ex1);
            }
            
            // Just creating a new input stream instead of dealing with mark and reset...
            return new FileInputStream(file);
        }
    }
}
