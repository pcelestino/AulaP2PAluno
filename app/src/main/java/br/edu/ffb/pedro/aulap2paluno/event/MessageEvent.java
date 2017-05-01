package br.edu.ffb.pedro.aulap2paluno.event;

public class MessageEvent {
    public static final String EXIT_APP = "Sair do app";
    public static final String SERVER_ERROR = "Erro no servidor";
    public final String message;

    public MessageEvent(String message) {
        this.message = message;
    }
}
