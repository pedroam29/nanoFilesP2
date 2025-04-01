package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import es.um.redes.nanoFiles.util.FileInfo;

public class PeerMessage {

	private byte opcode;

	/*
	 * TODO: (Boletín MensajesBinarios) Añadir atributos u otros constructores
	 * específicos para crear mensajes con otros campos, según sea necesario
	 * 
	 */
	// Campos comunes a algunos mensajes TLV.
	private byte[] hash; // Por ejemplo, usado en OPCODE_END_OF_FILE y OPCODE_DOWNLOAD.
	private byte[] file_name; // Para OPCODE_DOWNLOAD y OPCODE_UPLOAD_FILE.
	private byte[] file_data; // Para OPCODE_FILE.
	private byte numberOfServersThatHaveFile;
	private byte identifierServer;
	private byte[] downloadedFile = null;
	private long downloadedFileLength;

	// Campos para mensajes de operaciones (ej. GetChunk).
	private long offset; // Desplazamiento (8 bytes) para OPCODE_GET_CHUNK.
	private int chunkSize; // Tamaño del fragmento (4 bytes) para OPCODE_GET_CHUNKV

	public PeerMessage() {
		opcode = PeerMessageOps.OPCODE_INVALID_CODE;
	}

	public PeerMessage(byte op) {
		opcode = op;
	}

	public PeerMessage(byte op, byte[] hash, byte[] name) {
		opcode = op;
		this.hash = hash;
		this.file_name = name;
	}

	public PeerMessage(byte op, byte[] hashOrData) {
		opcode = op;
		if (op == PeerMessageOps.OPCODE_FILE)
			file_data = hashOrData;
		else
			hash = hashOrData;
	}

	// Constructor para mensajes de operación (GetChunk).
	public PeerMessage(byte op, long offset, int chunkSize) {
		opcode = op;
		this.offset = offset;
		this.chunkSize = chunkSize;
	}

	/*
	 * TODO: (Boletín MensajesBinarios) Crear métodos getter y setter para obtener
	 * los valores de los atributos de un mensaje. Se aconseja incluir código que
	 * compruebe que no se modifica/obtiene el valor de un campo (atributo) que no
	 * esté definido para el tipo de mensaje dado por "operation".
	 */
	// Getters y Setters
	public byte getOpcode() {
		return opcode;
	}

	public void setOpcode(byte opcode) {
		this.opcode = opcode;
	}

	public byte[] getHash() {
		return hash;
	}

	public void setHash(byte[] hash) {
		this.hash = hash;
	}

	public byte[] getFile_name() {
		return file_name;
	}

	public void setFile_name(byte[] file_name) {
		this.file_name = file_name;
	}

	public byte[] getFile_data() {
		return file_data;
	}

	public void setFile_data(byte[] file_data) {
		this.file_data = file_data;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public int getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}
	
	public int getNumberOfServersThatHaveFile() {
		return this.numberOfServersThatHaveFile;
	}
	
	public void setNumberOfServersThatHaveFile(byte n) {
		this.numberOfServersThatHaveFile = n;
	}
	
	public void setIdentifierServer(byte n) {
		this.identifierServer = n;
	}
	
	public byte getIdentifierServer() {
		return this.identifierServer;
	}
	public byte[] getDownloadedFile() {
		byte[] aux = new byte[(int) this.downloadedFileLength];
		System.arraycopy(this.downloadedFile, 0, aux, 0, (int) downloadedFileLength);
		return aux;
	}
	public long getDownloadedFileLength() {
		return this.downloadedFileLength;
	}

	/**
	 * Método de clase para parsear los campos de un mensaje y construir el objeto
	 * DirMessage que contiene los datos del mensaje recibido
	 * 
	 * @param data El array de bytes recibido
	 * @return Un objeto de esta clase cuyos atributos contienen los datos del
	 *         mensaje recibido.
	 * @throws IOException
	 */
	public static PeerMessage readMessageFromInputStream(DataInputStream dis) throws IOException {
		/*
		 * TODO: (Boletín MensajesBinarios) En función del tipo de mensaje, leer del
		 * socket a través del "dis" el resto de campos para ir extrayendo con los
		 * valores y establecer los atributos del un objeto DirMessage que contendrá
		 * toda la información del mensaje, y que será devuelto como resultado. NOTA:
		 * Usar dis.readFully para leer un array de bytes, dis.readInt para leer un
		 * entero, etc.
		 */
		PeerMessage message = new PeerMessage();
		byte opcode = dis.readByte();
		message.setOpcode(opcode);

		switch (opcode) {
		case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
			// Mensaje de control: No se requiere leer más datos.
			break;

		case PeerMessageOps.OPCODE_END_OF_FILE:
			// Formato TLV: [Opcode][Int:hashLength][byte[]:hash]
			int hashLength = dis.readInt();
			byte[] hashBuffer = new byte[hashLength];
			dis.readFully(hashBuffer);
			message.setHash(hashBuffer);
			break;
		case PeerMessageOps.OPCODE_DOWNLOAD:
			// Formato TLV:
			// [Opcode][Int:hashLength][byte[]:hash][Int:nameLength][byte[]:file_name]
			hashLength = dis.readInt();
			hashBuffer = new byte[hashLength];
			dis.readFully(hashBuffer);
			message.setHash(hashBuffer);

			int nameLength = dis.readInt();
			byte[] nameBuffer = new byte[nameLength];
			dis.readFully(nameBuffer);
			message.setFile_name(nameBuffer);
			break;
		case PeerMessageOps.OPCODE_FILE:
			// Formato TLV: [Opcode][Int:dataLength][byte[]:file_data]
			int dataLength = dis.readInt();
			byte[] dataBuffer = new byte[dataLength];
			dis.readFully(dataBuffer);
			message.setFile_data(dataBuffer);
			break;

		case PeerMessageOps.OPCODE_GET_CHUNK:
			// Formato Operaciones: [Opcode][long:offset][int:chunkSize]
			long offset = dis.readLong();
			int chunkSize = dis.readInt();
			message.setOffset(offset);
			message.setChunkSize(chunkSize);
			break;

		case PeerMessageOps.OPCODE_UPLOAD_FILE:
			// Formato TLV: [Opcode][short:nameLength][byte[]:file_name]
			int fileNameLength = dis.readShort();
			byte[] fileNameBuffer = new byte[fileNameLength];
			dis.readFully(fileNameBuffer);
			message.setFile_name(fileNameBuffer);
			break;

		default:
			System.err.println("PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: "
					+ PeerMessageOps.opcodeToOperation(opcode));
			System.exit(-1);
		}
		return message;
	}

	public void writeMessageToOutputStream(DataOutputStream dos) throws IOException {
		/*
		 * TODO (Boletín MensajesBinarios): Escribir los bytes en los que se codifica el
		 * mensaje en el socket a través del "dos", teniendo en cuenta opcode del
		 * mensaje del que se trata y los campos relevantes en cada caso. NOTA: Usar
		 * dos.write para leer un array de bytes, dos.writeInt para escribir un entero,
		 * etc.
		 */

		dos.writeByte(opcode);
		switch (opcode) {
		case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
			// Mensaje de control: Solo se escribe el opcode.
			break;

		case PeerMessageOps.OPCODE_END_OF_FILE:
			// [Opcode][Int:hashLength][byte[]:hash]
			dos.writeInt(hash.length);
			dos.write(hash);
			break;

		case PeerMessageOps.OPCODE_DOWNLOAD:
			// [Opcode][Int:hashLength][byte[]:hash][Int:nameLength][byte[]:file_name]
			dos.writeInt(hash.length);
			dos.write(hash);
			dos.writeInt(file_name.length);
			dos.write(file_name);
			break;

		case PeerMessageOps.OPCODE_FILE:
			// [Opcode][Int:dataLength][byte[]:file_data]
			dos.writeInt(file_data.length);
			dos.write(file_data);
			break;

		case PeerMessageOps.OPCODE_GET_CHUNK:
			// [Opcode][long:offset][int:chunkSize]
			dos.writeLong(offset);
			dos.writeInt(chunkSize);
			break;

		case PeerMessageOps.OPCODE_UPLOAD_FILE:
			// [Opcode][short:fileNameLength][byte[]:file_name]
			dos.writeShort(file_name.length);
			dos.write(file_name);
			break;

		default:
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
					+ PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}

}
