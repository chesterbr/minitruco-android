package me.chester.minitruco.server;

/*
 * Copyright © 2005-2012 Carlos Duarte do Nascimento "Chester" <cd@pobox.com>
 * Todos os direitos reservados.
 *
 * A redistribuição e o uso nas formas binária e código fonte, com ou sem
 * modificações, são permitidos contanto que as condições abaixo sejam
 * cumpridas:
 *
 * - Redistribuições do código fonte devem conter o aviso de direitos
 *   autorais acima, esta lista de condições e o aviso de isenção de
 *   garantias subseqüente.
 *
 * - Redistribuições na forma binária devem reproduzir o aviso de direitos
 *   autorais acima, esta lista de condições e o aviso de isenção de
 *   garantias subseqüente na documentação e/ou materiais fornecidos com
 *   a distribuição.
 *
 * - Nem o nome do Chester, nem o nome dos contribuidores podem ser
 *   utilizados para endossar ou promover produtos derivados deste
 *   software sem autorização prévia específica por escrito.
 *
 * ESTE SOFTWARE É FORNECIDO PELOS DETENTORES DE DIREITOS AUTORAIS E
 * CONTRIBUIDORES "COMO ESTÁ", ISENTO DE GARANTIAS EXPRESSAS OU TÁCITAS,
 * INCLUINDO, SEM LIMITAÇÃO, QUAISQUER GARANTIAS IMPLÍCITAS DE
 * COMERCIABILIDADE OU DE ADEQUAÇÃO A FINALIDADES ESPECÍFICAS. EM NENHUMA
 * HIPÓTESE OS TITULARES DE DIREITOS AUTORAIS E CONTRIBUIDORES SERÃO
 * RESPONSÁVEIS POR QUAISQUER DANOS, DIRETOS, INDIRETOS, INCIDENTAIS,
 * ESPECIAIS, EXEMPLARES OU CONSEQUENTES, (INCLUINDO, SEM LIMITAÇÃO,
 * FORNECIMENTO DE BENS OU SERVIÇOS SUBSTITUTOS, PERDA DE USO OU DADOS,
 * LUCROS CESSANTES, OU INTERRUPÇÃO DE ATIVIDADES), CAUSADOS POR QUAISQUER
 * MOTIVOS E SOB QUALQUER TEORIA DE RESPONSABILIDADE, SEJA RESPONSABILIDADE
 * CONTRATUAL, RESTRITA, ILÍCITO CIVIL, OU QUALQUER OUTRA, COMO DECORRÊNCIA
 * DE USO DESTE SOFTWARE, MESMO QUE HOUVESSEM SIDO AVISADOS DA
 * POSSIBILIDADE DE TAIS DANOS.
 *
 */

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Set;

import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Jogador;

/**
 * Representa um cliente conectado, dentro ou fora de um jogo.
 * <p>
 * A classe é capaz de processar os comandos do jogador, e, uma vez associada ao
 * jogo, interagir com ele.
 * <p>
 * A classe também responde ao comando <code>GET /applet.html</code> (na
 * verdade, a qualquer tipo de <code>GET</code>, mas é melhor usar esse para
 * compatibilidade futura), retornando o HTML que abre a applet hospedada em
 * <code>chester.inf.br</code>
 * <p>
 * Isso é feito para permitir que esta applet se conecte no servidor, sem
 * restrições de segurança (vide http://java.sun.com/sfaq/#socket).
 *
 * @author Chester
 */
public class JogadorConectado extends Jogador implements Runnable {

    public static final String IF_MODIFIED_SINCE_HTTP_HEADER = "If-Modified-Since: ";

    /**
     * Nomes de jogadores online (para evitar duplicidade)
     */
    private static final Set<String> nomes = new HashSet<String>();
    private final Socket cliente;
    private final String[] ARQUIVOS_PERMITIDOS_HTTP = {"/applet.html",
            "/miniTruco.jar", "/microemulator.jar", "/mtskin.jar",
            "/favicon.ico", "/english.jar", "/applet_en.html"};
    /**
     * Informa se o jogador está participando de um jogo
     */
    public boolean jogando = false;
    /**
     * Informa se o jogador autorizou o início da partida na sala
     */
    public boolean querJogar = false;

    private Sala sala;

    /**
     * Buffer de saída do jogador (para onde devemos "printar" os resultados dos
     * comandos)
     */
    private PrintStream out;

    /**
     * Cria um novo jogador
     *
     * @param cliente socket-cliente através do qual o jogador se conectou
     */
    public JogadorConectado(Socket cliente) {
        this.cliente = cliente;
    }

    /**
     * Verifica se um nome está em uso por algum jogador
     *
     * @param nome nome a verificar
     * @return true se já está em uso, false caso contrário
     */
    public static boolean isNomeEmUso(String nome) {
        return nomes.contains(nome.toUpperCase());
    }

    /*
     * public void println(String mensagem) { if (out!=null) {
     * out.println(mensagem); } }
     */

    /**
     * Impede que um nome seja usado
     *
     * @param nome
     */
    public static void bloqueiaNome(String nome) {
        nomes.add(nome.toUpperCase());
    }

    /**
     * Libera o uso de um nome
     *
     * @param nome
     */
    public static void liberaNome(String nome) {
        nomes.remove(nome.toUpperCase());
    }

    // TODO: ver se não vai fazer falta
    // @Override
    // public void jogadorAceito(Jogador j, Jogo jogo) {
    // println("Y " + j.getPosicao());
    // }

    /**
     * Envia uma linha de texto para o cliente (tipicamente o resultado de um
     * comando)
     *
     * @param linha linha de texto a enviar
     */
    public void println(String linha) {
        out.print(linha);
        out.print("\r\n");
        ServerLogger.evento(this, linha);
    }

    /**
     * Aguarda comandos do jogador e os executa
     */
    public void run() {
        ServerLogger.evento(this, "conectou");
        try {
            // Configura um timeout para evitar conexões presas
            ServerLogger.evento(this, "timeout antes:" + cliente.getSoTimeout());
            cliente.setSoTimeout(5000);
            ServerLogger.evento(this, "timeout depois:" + cliente.getSoTimeout());
            // Prepara o buffer de saída
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    cliente.getInputStream()));
            out = new PrintStream(cliente.getOutputStream());
            while (true) {
                String s = null;
                try {
                    s = in.readLine();
                } catch (SocketTimeoutException e) {
                    // A linha é só pra garantir que, no caso de uma conexão presa
                    // o teste abaixo dela dê erro (seja no if, seja exception)
                    println("");
                    if (!cliente.isConnected()) {
                        ServerLogger.evento("Desconexao detectada durante timeout");
                        return;
                    }
                    continue;
                }
                if (s == null) {
                    // Desconexão
                    return;
                }
                // Quebra a solicitação em tokens
                String[] args = s.split(" ");
                if (args.length == 0 || args[0].length() == 0) {
                    continue;
                }

                // Se for um pedido de browser (para um arquivo da applet),
                // serve e desconecta
                if (args[0].equals("GET") && (args.length > 1)) {
                    // Log do GET (incluindo headers)
                    do {
                        ServerLogger.evento(this, "]" + s);
                        s = in.readLine();
                        /*
                         * TODO Arrumar esse código, não funciona a contento //
                         * Se o cliente tiver uma cópia no cache, tenta usar if
                         * (s.startsWith(IF_MODIFIED_SINCE_HTTP_HEADER)) { try {
                         * Date dataMax = MiniTrucoServer.dfStartup .parse(s
                         * .substring(IF_MODIFIED_SINCE_HTTP_HEADER .length()));
                         * System.out.println(MiniTrucoServer.dataStartup);
                         * System.out.println(dataMax); if
                         * (!MiniTrucoServer.dataStartup.after(dataMax)) {
                         * ServerLogger.evento("304 Not Modified");
                         * out.println("HTTP/1.0 304 Not Modified");
                         * out.flush(); cliente.close(); return; } } catch
                         * (ParseException e) { // Se não conseguiu parsear,
                         * loga e desencana ServerLogger.evento(this,
                         * "!Cabecalho invalido: " + s + ". Erro: " +
                         * e.getMessage()); } }
                         */
                    } while ((s != null) && (!s.equals("")));
                    // Se livra dos parâmetros
                    String nomeArq = args[1];
                    int fimNome = nomeArq.indexOf('?');
                    if (fimNome != -1)
                        nomeArq = nomeArq.substring(0, fimNome);
                    // Serve o arquivo
                    serveArquivosApplet(nomeArq, out);
                    cliente.close();
                    return;
                }

                // Encontra a implementação do comando solicitado e chama
                if ((args[0] == null) || (args[0].length() != 1)) {
                    continue;
                }
                char comando = Character.toUpperCase(args[0].charAt(0));
                try {
                    Comando c = (Comando) Class.forName(
                            "me.chester.minitruco.server.Comando"
                                    + comando).newInstance();
                    c.executa(args, this);
                } catch (ClassNotFoundException e) {
                    println("X CI");
                } catch (InstantiationException e) {
                    println("X CI");
                } catch (IllegalAccessException e) {
                    println("X CI");
                }
            }
        } catch (IOException e) {
            // Meio improvável de rolar, however...
            ServerLogger.evento(e, "Erro de I/O no loop principal do jogador");
        } finally {
            // Ao final, remove o usuário de qualquer sala em que esteja,
            // remove seu nome da lista de nomes usados e loga
            if (getSala() != null) {
                (new ComandoS()).executa(null, this);
            }
            if (!getNome().equals("unnamed")) {
                liberaNome(getNome());
            }
            ServerLogger.evento(this, "desconectou");
        }

    }

    /**
     * Serve (em formato HTTP) os arquivos que permitem inicializar a applet.
     * <p>
     *
     * @param nomeArq nome do arquivo a ser servido (precedido de "/"), que deve
     *                estar disponível no classpath e na lista de arquivos
     *                permitidos
     * @param out     stream para onde o HTML será servido
     */
    private void serveArquivosApplet(String nomeArq, PrintStream out) {
        try {

            if (nomeArq.equals("/")) {
                nomeArq = "/applet.html";
            }

            boolean permitido = false;
            for (int i = 0; i < ARQUIVOS_PERMITIDOS_HTTP.length; i++) {
                if (ARQUIVOS_PERMITIDOS_HTTP[i].equals(nomeArq)) {
                    permitido = true;
                    break;
                }
            }

            InputStream is = getClass().getResourceAsStream(nomeArq);

            if (!permitido || (is == null)) {
                ServerLogger.evento("404 Not Found: " + nomeArq);
                out.println("HTTP/1.0 404 Not Found");
                out.println("Content-Length: 14");
                out.println();
                out.println("Nao Encontrado");
                return;
            }

            ServerLogger.evento("200 Ok: " + nomeArq);
            BufferedInputStream bis = new BufferedInputStream(is);
            out.println("HTTP/1.0 200 OK");
            if (nomeArq.endsWith(".html")) {
                out.println("Content-Type: text/html");
            } else if (nomeArq.endsWith(".jar")) {
                out.println("Content-Type: application/x-java-archive");
            } else if (nomeArq.endsWith(".ico")) {
                out.println("Content-Type: image/x-icon");
            }
            out.println("Server: miniTrucoServer/"
                    + MiniTrucoServer.VERSAO_SERVER);
            // Como não temos como recuperar as datas dos arquivos no JAR, vamos
            // usar a data de startup do servidor (que é razoavelmente estável e
            // só cresce nas CNTP)
            out.println("Last-modified:" + MiniTrucoServer.strDataStartup);
            out.println("Content-Length: " + bis.available());
            out.println();
            byte[] buf = new byte[4096];
            int numBytes;
            while ((numBytes = bis.read(buf)) != -1) {
                out.write(buf, 0, numBytes);
            }
            bis.close();
            is.close();
        } catch (IOException e) {
            ServerLogger.evento(e, "Erro de I/O ao servir " + nomeArq);
        }
    }

    @Override
    public void cartaJogada(Jogador j, Carta c) {
        String param;
        if (c.isFechada()) {
            if (j.equals(this)) {
                param = " " + c + " T";
            } else {
                param = "";
            }
        } else {
            param = " " + c;
        }
        println("J " + j.getPosicao() + param);
    }

    @Override
    public void inicioMao() {
        StringBuilder comando = new StringBuilder("M");
        for (int i = 0; i <= 2; i++)
            comando.append(" " + getCartas()[i]);
        if (!getSala().manilhaVelha) {
            comando.append(" " + getSala().getJogo().cartaDaMesa);
        }
        println(comando.toString());
    }

    @Override
    public void inicioPartida(int placarEquipe1, int placarEquipe2) {
        // TODO comparar com bluetooth (todos os eventos, alias)
        println("P " + getPosicao());
    }

    @Override
    public void vez(Jogador j, boolean podeFechada) {
        println("V " + j.getPosicao() + ' ' + (podeFechada ? 'T' : 'F'));
    }

    @Override
    public void pediuAumentoAposta(Jogador j, int valor) {
        println("T " + j.getPosicao() + ' ' + valor);
    }

    @Override
    public void aceitouAumentoAposta(Jogador j, int valor) {
        println("D " + j.getPosicao() + ' ' + valor);
    }

    @Override
    public void recusouAumentoAposta(Jogador j) {
        println("C " + j.getPosicao());
    }

    @Override
    public void rodadaFechada(int numRodada, int resultado,
                              Jogador jogadorQueTorna) {
        println("R " + resultado + ' ' + jogadorQueTorna.getPosicao());
    }

    @Override
    public void maoFechada(int[] pontosEquipe) {
        println("O " + pontosEquipe[0] + ' ' + pontosEquipe[1]);
    }

    @Override
    public void jogoFechado(int numEquipeVencedora) {
        desvinculaJogo();
        println("G " + numEquipeVencedora);
    }

    @Override
    public void decidiuMao11(Jogador j, boolean aceita) {
        println("H " + j.getPosicao() + (aceita ? " T" : " F"));
    }

    @Override
    public void informaMao11(Carta[] cartasParceiro) {
        StringBuilder sbComando = new StringBuilder("F ");
        for (int i = 0; i <= 2; i++) {
            sbComando.append(cartasParceiro[i]);
            if (i != 2)
                sbComando.append(' ');
        }
        println(sbComando.toString());
    }

    @Override
    public void jogoAbortado(int posicao) {
        desvinculaJogo();
        println("A " + posicao);
    }

    /**
     * Desvincula o jogo do jogador, e, se necessário, da sala
     */
    private synchronized void desvinculaJogo() {
        querJogar = false;
        jogando = false;
        Sala s = getSala();
        if (s != null)
            s.liberaJogo();
    }

    /**
     * Recupera a sala em que o jogado restá
     *
     * @return objeto representando a sala, ou null se estiver fora de uma sala
     */
    public Sala getSala() {
        return sala;
    }

    /**
     * Associa o jogador com a sala. NÃO deve ser usado diretamente (ao invés disso,
     * use Sala.adiciona() e Sala.remove())
     */
    public void setSala(Sala sala) {
        this.sala = sala;
    }

    @Override
    /**
     * Atribui um nome ao jogador (apenas se não houver outro com o mesmo nome)
     */
    public synchronized void setNome(String nome) {
        // Se já existir, desencana
        if (isNomeEmUso(nome)) {
            return;
        }
        // Se já tinha um nome, libera o seu uso
        if (!this.getNome().equals("unnamed")) {
            liberaNome(this.getNome());
        }
        // Seta o novo nome e evita novos usos
        super.setNome(nome);
        bloqueiaNome(nome);
    }

    public String getIp() {
        return cliente.getInetAddress().getHostAddress();
    }

}
