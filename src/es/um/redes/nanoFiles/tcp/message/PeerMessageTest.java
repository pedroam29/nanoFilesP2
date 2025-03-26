package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class PeerMessageTest {

    public static void main(String[] args) throws IOException {
        // Para cada tipo de mensaje se utiliza un fichero temporal distinto.
        testFileNotFound("peermsg_fileNotFound.bin");
        testEndOfFile("peermsg_endOfFile.bin");
        testDownload("peermsg_download.bin");
        testFile("peermsg_file.bin");
        testGetChunk("peermsg_getChunk.bin");
        testUploadFile("peermsg_uploadFile.bin");
    }
    
    private static void testFileNotFound(String fileName) throws IOException {
        System.out.println("Probando OPCODE_FILE_NOT_FOUND...");
        PeerMessage msgOut = new PeerMessage(PeerMessageOps.OPCODE_FILE_NOT_FOUND);
        writeAndTest(msgOut, fileName, "FILE_NOT_FOUND");
    }
    
    private static void testEndOfFile(String fileName) throws IOException {
        System.out.println("Probando OPCODE_END_OF_FILE...");
        // Por ejemplo, simulamos un hash con 4 bytes.
        byte[] hash = new byte[] { 10, 20, 30, 40 };
        PeerMessage msgOut = new PeerMessage(PeerMessageOps.OPCODE_END_OF_FILE, hash);
        writeAndTest(msgOut, fileName, "END_OF_FILE", hash, null, 0, 0);
    }
    
    private static void testDownload(String fileName) throws IOException {
        System.out.println("Probando OPCODE_DOWNLOAD...");
        // Se envían tanto un hash como el nombre del archivo.
        byte[] hash = new byte[] { 1, 2, 3, 4 };
        byte[] fileNameBytes = "nanoFilesP2P.zip".getBytes();
        PeerMessage msgOut = new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD, hash, fileNameBytes);
        writeAndTest(msgOut, fileName, "DOWNLOAD", hash, fileNameBytes, 0, 0);
    }
    
    private static void testFile(String fileName) throws IOException {
        System.out.println("Probando OPCODE_FILE...");
        // Simulamos unos datos de fichero.
        byte[] fileData = new byte[] { 50, 51, 52, 53, 54 };
        PeerMessage msgOut = new PeerMessage(PeerMessageOps.OPCODE_FILE, fileData);
        writeAndTest(msgOut, fileName, "FILE", null, null, 0, 0);
    }
    
    private static void testGetChunk(String fileName) throws IOException {
        System.out.println("Probando OPCODE_GET_CHUNK...");
        long offset = 65536L;
        int chunkSize = 131072;
        PeerMessage msgOut = new PeerMessage(PeerMessageOps.OPCODE_GET_CHUNK, offset, chunkSize);
        writeAndTest(msgOut, fileName, "GET_CHUNK", null, null, offset, chunkSize);
    }
    
    private static void testUploadFile(String fileName) throws IOException {
        System.out.println("Probando OPCODE_UPLOAD_FILE...");
        // Para upload file solo se envía el nombre del fichero en formato TLV (short longitud + bytes)
        byte[] fileNameBytes = "nanoFilesP2P.zip".getBytes();
        PeerMessage msgOut = new PeerMessage(PeerMessageOps.OPCODE_UPLOAD_FILE, null, fileNameBytes);
        writeAndTest(msgOut, fileName, "UPLOAD_FILE", null, fileNameBytes, 0, 0);
    }
    
    /**
     * Escribe el mensaje en un fichero, lo lee y compara los atributos.
     * Los parámetros opcionales (hash, fileName, offset y chunkSize) se usan para comparar
     * según el tipo de mensaje.
     */
    private static void writeAndTest(PeerMessage msgOut, String fileName, String type,
                                     byte[] expectedHash, byte[] expectedFileName,
                                     long expectedOffset, int expectedChunkSize) throws IOException {
        // Escribe el mensaje en el fichero
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(fileName));
        msgOut.writeMessageToOutputStream(dos);
        dos.close();
        
        // Lee el mensaje desde el fichero
        DataInputStream dis = new DataInputStream(new FileInputStream(fileName));
        PeerMessage msgIn = PeerMessage.readMessageFromInputStream(dis);
        dis.close();
        
        // Comprobación: opcode
        if (msgOut.getOpcode() != msgIn.getOpcode()) {
            System.err.println(type + ": Opcode does not match!");
        }
        
        // Comprobaciones adicionales según el tipo de mensaje
        switch (msgOut.getOpcode()) {
            case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
                // No hay más datos a comparar
                break;
            case PeerMessageOps.OPCODE_END_OF_FILE:
                if (!Arrays.equals(expectedHash, msgIn.getHash())) {
                    System.err.println(type + ": Hash does not match!");
                }
                break;
            case PeerMessageOps.OPCODE_DOWNLOAD:
                if (!Arrays.equals(expectedHash, msgIn.getHash())) {
                    System.err.println(type + ": Hash does not match!");
                }
                if (!Arrays.equals(expectedFileName, msgIn.getFile_name())) {
                    System.err.println(type + ": File name does not match!");
                }
                break;
            case PeerMessageOps.OPCODE_FILE:
                if (!Arrays.equals(msgOut.getFile_data(), msgIn.getFile_data())) {
                    System.err.println(type + ": File data does not match!");
                }
                break;
            case PeerMessageOps.OPCODE_GET_CHUNK:
                if (msgOut.getOffset() != msgIn.getOffset()) {
                    System.err.println(type + ": Offset does not match!");
                }
                if (msgOut.getChunkSize() != msgIn.getChunkSize()) {
                    System.err.println(type + ": Chunk size does not match!");
                }
                break;
            case PeerMessageOps.OPCODE_UPLOAD_FILE:
                if (!Arrays.equals(expectedFileName, msgIn.getFile_name())) {
                    System.err.println(type + ": File name does not match!");
                }
                break;
            default:
                System.err.println(type + ": Unknown opcode during test.");
        }
        System.out.println(type + " test passed.");
    }
    
    // Sobrecarga para mensajes que no requieren comparación de campos adicionales.
    private static void writeAndTest(PeerMessage msgOut, String fileName, String type) throws IOException {
        writeAndTest(msgOut, fileName, type, null, null, 0, 0);
    }
}
