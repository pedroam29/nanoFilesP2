package es.um.redes.nanoFiles.udp.message;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Clase que modela los mensajes del protocolo de comunicación entre pares para
 * implementar el explorador de ficheros remoto (servidor de ficheros). Estos
 * mensajes son intercambiados entre las clases DirectoryServer y
 * DirectoryConnector, y se codifican como texto en formato "campo:valor".
 * 
 * @author rtitos
 *
 */
public class DirMessage {
	public static final int PACKET_MAX_SIZE = 65507; // 65535 - 8 (UDP header) - 20 (IP header)

	private static final char DELIMITER = ':'; // Define el delimitador
	private static final char END_LINE = '\n'; // Define el carácter de fin de línea

	/**
	 * Nombre del campo que define el tipo de mensaje (primera línea)
	 */
	private static final String FIELDNAME_OPERATION = "operation";
	/*
	 * TODO: (Boletín MensajesASCII) Definir de manera simbólica los nombres de
	 * todos los campos que pueden aparecer en los mensajes de este protocolo
	 * (formato campo:valor)
	 */
	private static final String FIELDNAME_PROTOCOL = "protocol";

	private static final String FIELDNAME_FILES = "files";
	private static final String FIELDNAME_HASH = "hash";
	private static final String FIELDNAME_PORT = "port";
	private static final String FIELDNAME_SERVE_RESPONSE = "serveResponse";
	private static final String FIELDNAME_SERVE = "serve";
	
	/** 
	 * Tipo del mensaje, de entre los tipos definidos en PeerMessageOps.
	 */
	private String operation = DirMessageOps.OPERATION_INVALID;
	/**
	 * Identificador de protocolo usado, para comprobar compatibilidad del directorio.
	 */
	private String protocolId;
	/*
	 * TODO: (Boletín MensajesASCII) Crear un atributo correspondiente a cada uno de
	 * los campos de los diferentes mensajes de este protocolo.
	 */
	private List<FileInfo> files;
	private String hash;
	private int port;
	private InetSocketAddress[] serverList;
	private int serverPort;
	private String filenameSubstring;
	private boolean publishResponse;
	
	public DirMessage(String op) {
		operation = op;
	}

	/*
	 * TODO: (Boletín MensajesASCII) Crear diferentes constructores adecuados para
	 * construir mensajes de diferentes tipos con sus correspondientes argumentos
	 * (campos del mensaje)
	 */

	public DirMessage (String op, String idProtocol) {
		operation = op;
		protocolId = idProtocol;
	}

	public DirMessage (String op, String idProtocol, int port, FileInfo[] files) {
		operation = op;
		protocolId = idProtocol;
		this.files = new LinkedList<FileInfo>(Arrays.asList(files));
		setServerPort(port); 
	}
	
	public DirMessage (String op, FileInfo[] files) {
		operation = op;
		this.files = new LinkedList<FileInfo>(Arrays.asList(files));
	}
	public DirMessage (String op, String idProtocol, String filename) {
		operation = op;
		protocolId = idProtocol;
		setFilenameSubstring(filename);
	}
	
	
	
	
	
	public String getOperation() {
		return operation;
	}

	/*
	 * TODO: (Boletín MensajesASCII) Crear métodos getter y setter para obtener los
	 * valores de los atributos de un mensaje. Se aconseja incluir código que
	 * compruebe que no se modifica/obtiene el valor de un campo (atributo) que no
	 * esté definido para el tipo de mensaje dado por "operation".
	 */
	public void setProtocolID(String protocolIdent) {
		if (!operation.equals(DirMessageOps.OPERATION_PING)) {
			throw new RuntimeException(
					"DirMessage: setProtocolId called for message of unexpected type (" + operation + ")");
		}
		protocolId = protocolIdent;
	}

	public String getProtocolId() {
		return protocolId;
	}

	public FileInfo[] getFiles() {
		if (files != null)
			return files.toArray(new FileInfo[0]);
		return null;
	}
	
	public void setFiles(List<FileInfo> files) {
		this.files = files;
	}
	
	public void addFiles(FileInfo file) {
		this.files.add(file);
	}
	
	public boolean isFilesNull() {
		if(this.files == null) {
			return true;
		}
		return false;
	}
	
	
	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getHash() {
		return hash;
	}
	
	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	} 
	public boolean isPublishResponse() {
        return publishResponse;
    }
	public void setPublishResponse(boolean publishResponse) {
        this.publishResponse = publishResponse;
    }
	
	public InetSocketAddress[] getServerList() {
		return serverList;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getFilenameSubstring() {
		return filenameSubstring;
	}

	public void setFilenameSubstring(String filenameSubstring) {
		this.filenameSubstring = filenameSubstring;
	}
	
	
	
	/**
	 * Método que convierte un mensaje codificado como una cadena de caracteres, a
	 * un objeto de la clase PeerMessage, en el cual los atributos correspondientes
	 * han sido establecidos con el valor de los campos del mensaje.
	 * 
	 * @param message El mensaje recibido por el socket, como cadena de caracteres
	 * @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores,
	 *         etc.)
	 */
	public static DirMessage fromString(String message) {
		/*
		 * TODO: (Boletín MensajesASCII) Usar un bucle para parsear el mensaje línea a
		 * línea, extrayendo para cada línea el nombre del campo y el valor, usando el
		 * delimitador DELIMITER, y guardarlo en variables locales.
		 */

		// System.out.println("DirMessage read from socket:");
		// System.out.println(message);
		String[] lines = message.split(END_LINE + "");
		// Local variables to save data during parsing
		DirMessage m = null;

		for (String line : lines) {
			int idx = line.indexOf(DELIMITER); // Posición del delimitador
			String fieldName = line.substring(0, idx); // minúsculas
			String value = line.substring(idx + 1).trim();

			switch (fieldName) {
			case FIELDNAME_OPERATION: {
				assert (m == null);
				m = new DirMessage(value);
				break;
			}
			
			case FIELDNAME_PROTOCOL:{
				m.setProtocolID(value);
				break;
			}
			case FIELDNAME_FILES: {
				if (m.isFilesNull()) {
					m.setFiles(new LinkedList<FileInfo>());
				}
				String[] parts = value.split(",");
				assert (parts.length == 3);
				FileInfo file = new FileInfo(parts[0], parts[1], Long.parseLong(parts[2]), "");
				m.addFiles(file);
				break;

			}
			
			case FIELDNAME_PORT: {
				m.setPort(Integer.parseInt(value));
				break;
			}

			case FIELDNAME_SERVE_RESPONSE: {
                assert (m.getOperation().equals(DirMessageOps.OPERATION_SERVE_RESPONSE));
                m.setPublishResponse(Boolean.parseBoolean(value));
                break;
            }
			default:
				System.err.println("PANIC: DirMessage.fromString - message with unknown field name " + fieldName);
				System.err.println("Message was:\n" + message);
				System.exit(-1);
			}
		}



		return m;
	}

	

	/**
	 * Método que devuelve una cadena de caracteres con la codificación del mensaje
	 * según el formato campo:valor, a partir del tipo y los valores almacenados en
	 * los atributos.
	 * 
	 * @return La cadena de caracteres con el mensaje a enviar por el socket.
	 */
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append(FIELDNAME_OPERATION + DELIMITER + operation + END_LINE); // Construimos el campo
		/*
		 * TODO: (Boletín MensajesASCII) En función de la operación del mensaje, crear
		 * una cadena la operación y concatenar el resto de campos necesarios usando los
		 * valores de los atributos del objeto.
		 */
		switch(operation) {
		case DirMessageOps.OPERATION_PING:{
			sb.append(FIELDNAME_PROTOCOL + DELIMITER + protocolId + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_FILELIST_RESPONSE: {
			if (this.files == null)
				sb.append("No contiene ficheros");
			else
				for (FileInfo f : this.files) {
					sb.append(FIELDNAME_FILES + DELIMITER + f.fileHash + "," + f.fileName + "," + f.fileSize + END_LINE);
				}
			break;
		}
		case DirMessageOps.OPERATION_SERVE_RESPONSE: {
			//Le devuelve una respuesta en funcion de si ha sido o no un exito la publicacion de ficheros
            sb.append(FIELDNAME_SERVE_RESPONSE+ DELIMITER + publishResponse + END_LINE);
		}
		}
		sb.append(END_LINE); // Marcamos el final del mensaje
		return sb.toString();
	}


}
