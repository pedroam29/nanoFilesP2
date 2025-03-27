package es.um.redes.nanoFiles.udp.message;

public class DirMessageOps {

	/*
	 * TODO: (Boletín MensajesASCII) Añadir aquí todas las constantes que definen
	 * los diferentes tipos de mensajes del protocolo de comunicación con el
	 * directorio (valores posibles del campo "operation").
	 */
	public static final String OPERATION_INVALID = "invalid_operation";
	public static final String OPERATION_PING = "ping";
	public static final String OPERATION_FILELIST = "filelist";
	public static final String OPERATION_SERVE = "serve";
	public static final String OPERATION_DOWNLOAD = "download";

	public static final String OPERATION_PING_OK = "pingOk";
	public static final String OPERATION_PING_BAD = "pingBad";

	public static final String OPERATION_FILELIST_RESPONSE = "filelist_response";

	public static final String OPERATION_SERVE_RESPONSE = "serveResponse";

	public static final String OPERATION_DOWNLOAD_OK = "downloadOk";
	public static final String OPERATION_DOWNLOAD_BAD = "downloadBad";

}
