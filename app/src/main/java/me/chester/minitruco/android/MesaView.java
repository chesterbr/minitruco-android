package me.chester.minitruco.android;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import me.chester.minitruco.R;
import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Jogador;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Representa visualmente o andamento de uma partida, permitindo que o usuário
 * interaja.
 * <p>
 * Os eventos visuais são disparados pelo <code>JogadorHumano</code>, e as
 * acões do usuário são repassadas para a <code>Partida</code> (ambas as
 * referências são obtidas da <code>TrucoActivity</code>).
 *
 * Para simplificar o acesso, alguns métodos/propriedades são static - o que só
 * reitera que só deve existir uma instância desta View.
 * <p>
 */
public class MesaView extends View {

    public static final int FPS_ANIMANDO = 60;
    public static final int FPS_PARADO = 4;

    /**
     * Indica que é a vez do humano, e ele pode jogar
     */
    public static final int STATUS_VEZ_HUMANO_OK = 1;

    /**
     * Indica que é a vez de outro jogador
     */
    public static final int STATUS_VEZ_OUTRO = 0;

    /**
     * Indica que é a vez do humano, e ele está aguardando resposta (ex.: de
     * aumento) para jogar
     */
    public static final int STATUS_VEZ_HUMANO_AGUARDANDO = -1;

    /**
     * Cartas que estão na mesa, na ordem de empilhamento. cartas[0] é o vira,
     * cartas[1..3] são o baralho decorativo, cartas[4..6] são as do jogador na
     * posição 1 (inferior), cartas[7..9] o jogador 2 e assim por diante para os
     * jogadores 3 e 4.
     * <p>
     * TODO: refatorar esses magic numbers para algo melhor.
     */
    public final CartaVisual[] cartas = new CartaVisual[16];

    /**
     * Guarda as cartas que foram jogadas pelos jogadores (para saber em que
     * ordem desenhar)
     */
    private final Vector<CartaVisual> cartasJogadas = new Vector<>(12);

    private static final Random rand = new Random();
    private final Paint paintPergunta = new Paint();
    private float density;
    private Drawable indicadorDrag;
    private boolean mostrarPerguntaMaoDeX = false;
    private boolean mostrarPerguntaAumento = false;
    private boolean mostrarBotaoAumento = false;
    private boolean mostrarBotaoAbertaFechada = false;
    private String perguntaAumento;
    private String perguntaMaoDeX;
    public boolean vaiJogarFechada;
    private int valorProximaAposta;
    protected int velocidade = 1;
    private int posicaoVez;
    private int corFundoCartaBalao = Color.WHITE;
    private TrucoActivity trucoActivity;
    private Rect rectPergunta;
    private RectF rectBotaoSim;
    private RectF rectBotaoNao;
    private RectF rectBotaoAumento;
    private RectF rectBotaoAbertaFechada;
    private float tamanhoFonte;
    private float divisorTamanhoFonte = 20;
    private int ultimoyDaPergunta = -1;

    /**
     * É true se a view já está pronta para responder a solicitações da partida
     * (mover cartas, acionar balões, etc)
     */
    private boolean inicializada = false;

    /**
     * Guarda o texto do botão de aumento para cada valor de aumento (ex.:
     * "truco" para 3, "seis" para 6, etc)
     */
    private final HashMap<Integer, String> textosBotaoAumento = new HashMap<>();

    /**
     * Guarda o índice da última frase escolhida para cada tipo de balão (ex.:
     * balão de pedido de aumento, balão de aceite, etc.), para evitar repetir
     * a mesma frase imediatamente.
     */
    private final HashMap<String, Integer> ultimaFrase = new HashMap<>();

    public boolean isInicializada() {
        return inicializada;
    }

    /**
     * Posição do baralho (decorativo) na mesa
     */
    private int topBaralho, leftBaralho;

    /**
     * Timestamp em que as animações em curso irão acabar
     */
    private long animandoAte = System.currentTimeMillis();

    /**
     * Diz se é a vez do jogador humano dessa mesa ou de outro, e, no primeiro
     * caso se está aguardando resposta de truco
     */
    private int statusVez = 0;

    /**
     * A mesa não é mais responsável pelos indicadores de rodada, mas precisa
     * saber quando um deles termina de "piscar" para dar seguimento
     */
    private boolean isRodadaPiscando;
    private long rodadaPiscaAte = System.currentTimeMillis();

    /**
     * Carta que "fez" a última rodada (para fins de destaque)
     */
    private CartaVisual cartaQueFez;

    private int posicaoBalao = 1;
    private long mostraBalaoAte = System.currentTimeMillis();
    String fraseBalao = null;
    private boolean visivel = false;

    /**
     * Thread/runnable que faz as animações acontecerem (invalidando
     * o display -> forçando um redraw várias vezes por segundo)
     * <p>
     */
    final Thread threadAnimacao = new Thread(new Runnable() {

        public void run() {
            int tempoEntreFramesAnimando = 1000 / FPS_ANIMANDO;
            int tempoEntreFramesParado = 1000 / FPS_PARADO;
            // Aguarda a partida existir
            while (trucoActivity.partida == null) {
                sleep(200);
            }
            // Roda até a activity-mãe se encerrar, num frame rate que depende
            // de estarmos animando algo ou não (mas sempre atualiza, pra não
            // perder mudanças por algum arredondaento de ms ou por serem
            // instantâneas)
            while (!trucoActivity.isFinishing()) {
                if (visivel) {
                    postInvalidate();
                }
                if (calcTempoAteFimAnimacaoMS() >= 0) {
                    sleep(tempoEntreFramesAnimando);
                } else {
                    sleep(tempoEntreFramesParado);
                }
            }
        }

        private void sleep(int tempoMS) {
            try {
                Thread.sleep(tempoMS);
            } catch (InterruptedException e) {
                // Não faz nada; vamos interromper esse sleep sempre que
                // uma animação começar!
            }

        }
    });

    public MesaView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public MesaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MesaView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.density = context.getResources().getDisplayMetrics().density;
        this.indicadorDrag = getResources().getDrawable(R.drawable.indicador_drag);
    }

    public void setTrucoActivity(TrucoActivity trucoActivity) {
        this.trucoActivity = trucoActivity;
    }

    public void setIndiceDesenhoCartaFechada(int indice) {
        CartaVisual.setIndiceDesenhoCartaFechada(indice);
    }

    /**
     * Ajusta (aumenta) o tamanho da fonte dos textos dos balões, botões, etc
     *
     * @param escala valor de 1 (escala normal) a 8 (pode até ser mais, mas
     *               aí a interface quebra muito; mesmo 8 já fica estourado em
     *               telas muito pequenas); não é exatamente linear
     */
    public void setEscalaFonte(int escala) {
        divisorTamanhoFonte = 21f - escala;
    }

    /**
     * Informa à mesa que uma animação começou (garantindo refreshes da tela
     * enquanto ela durar).
     *
     * @param fim timestamp de quando a animação vai acabar
     */
    public void notificaAnimacao(long fim) {
        if (animandoAte < fim) {
            animandoAte = fim;
        }
        threadAnimacao.interrupt(); // Hora de acordar e subir o frame rate!
    }

    /**
     * Coloca a thread chamante em sleep até que as animações acabem.
     */
    public void aguardaFimAnimacoes() {
        long milisecAteFimAnimacao;
        while ((milisecAteFimAnimacao = animandoAte - System.currentTimeMillis()) > 0) {
            try {
                Thread.sleep(milisecAteFimAnimacao);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    /**
     * Ajusta o tamanho das cartas e sua posição de acordo com a resolução.
     * <p>
     * Na primeira chamada, incializa as cartas e dá início à partida (tem que
     * ser feito aqui porque não é uma boa idéia começar sem saber onde as
     * coisas vão aparecer)
     */
    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {

        // Ajusta o tamanho da carta (tudo depende dele) ao da mesa e os
        // "pontos de referência" importantes (baralho decorativo, tamanho do
        // texto, etc.)
        CartaVisual.ajustaTamanho(w, h);
        int delta = CartaVisual.altura / 24;
        leftBaralho = w - CartaVisual.largura - delta * 3;
        topBaralho = delta;
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        tamanhoFonte = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_PX,
            Math.min(w, h) / divisorTamanhoFonte,
            displayMetrics
        );

        // Na primeira chamada (inicialização), instanciamos as cartas
        if (!inicializada) {
            for (int i = 0; i < cartas.length; i++) {
                cartas[i] = new CartaVisual(this, leftBaralho, topBaralho, null, corFundoCartaBalao);
                cartas[i].movePara(leftBaralho, topBaralho);
            }
            cartas[0].visible = false;
        }

        // Define posição e tamanho da caixa de pergunta e seus botões
        int larguraPergunta = (int) (tamanhoFonte * 11);
        int alturaPergunta = (int) (larguraPergunta / 2.2f);
        int topPergunta = (h - alturaPergunta) / 2;
        int leftPergunta = (w - larguraPergunta) / 2;
        rectPergunta = new Rect(leftPergunta, topPergunta, leftPergunta + larguraPergunta, topPergunta + alturaPergunta);
        int alturaBotao = (int) (alturaPergunta * 0.30f);
        int margemBotao = (int) (8 * density);
        int bottomBotao = rectPergunta.bottom - margemBotao;
        int topBotao = bottomBotao - alturaBotao;
        int larguraBotao =  larguraPergunta / 2 - margemBotao;
        rectBotaoSim = new RectF(
            leftPergunta + margemBotao,
            topBotao,
            leftPergunta + larguraBotao,
            bottomBotao);
        rectBotaoNao = new RectF(
            rectBotaoSim.right + margemBotao,
            topBotao,
            rectBotaoSim.right + margemBotao + larguraBotao,
            bottomBotao);

        // Define posição e tamanho dos botões de aumento e carta aberta/fechada
        // (são quadrados cujo lado é a altura da carta)
        rectBotaoAumento = new RectF(
            margemBotao,
            h - margemBotao - CartaVisual.altura,
            margemBotao + CartaVisual.altura,
            h - margemBotao
        );
        rectBotaoAbertaFechada = new RectF(
            w - margemBotao - CartaVisual.altura,
            rectBotaoAumento.top,
            w - margemBotao,
            rectBotaoAumento.bottom
        );

        // Posiciona o vira e as cartas decorativas do baralho, que são fixos
        cartas[0].movePara(leftBaralho, topBaralho);
        cartas[1].movePara(leftBaralho + 2 * delta, topBaralho + 2 * delta);
        cartas[2].movePara(leftBaralho + delta, topBaralho + delta);
        cartas[3].movePara(leftBaralho, topBaralho);

        if (!inicializada) {
            threadAnimacao.start();
        } else {
            // Rolou um resize, reposiciona as cartas não-decorativas
            for (int i = 0; i <= 15; i++) {
                CartaVisual cv = cartas[i];
                if (cv != null) {
                    cv.resetBitmap();
                    if (i >= 4) {
                        int numJogador = (i - 1) / 3;
                        if (trucoActivity != null && trucoActivity.partida != null && trucoActivity.partida.finalizada) {
                            cv.movePara(leftBaralho, topBaralho);
                        } else if (cv.descartada) {
                            cv.movePara(calcPosLeftDescartada(numJogador), calcPosTopDescartada(numJogador));
                        } else {
                            int pos = (i - 1) % 3;
                            cv.movePara(calcPosLeftCarta(numJogador, pos), calcPosTopCarta(numJogador, pos));
                        }
                    }
                }
            }
        }

        if (!inicializada && isInEditMode()) {
            velocidade = 4;
            distribuiMao();
        }

        inicializada = true;
    }

    /**
     * Recupera a carta visual correspondente a uma carta da partida.
     *
     * @param c carta da partida
     * @return Carta visual com o valor desta, ou <code>null</code> se não achar
     */
    public CartaVisual getCartaVisual(Carta c) {
        for (CartaVisual cv : cartas) {
            if (c != null && c.equals(cv)) {
                return cv;
            }
        }
        return null;
    }

    /**
     * Atualiza o resultado de uma rodada, destacando a carta vencedora e
     * aguardando o tempo em que o placar vai piscar o marcador de rodada
     *
     * @param numRodada       rodada que finalizou
     * @param resultado       (0 = nenhum; 1 = vitória, 2 = derrota, 3 = empate)
     * @param jogadorQueTorna jogador cuja carta venceu a rodada
     */
    public void atualizaResultadoRodada(int numRodada, int resultado, Jogador jogadorQueTorna) {
        aguardaFimAnimacoes();
        if (resultado != 3) {
            cartaQueFez = getCartaVisual(trucoActivity.partida.getCartasDaRodada(numRodada)[jogadorQueTorna.getPosicao() - 1]);
            if (cartaQueFez != null) {
                cartaQueFez.destacada = true;
            }
        }
        for (CartaVisual c : cartas) {
            c.escura = c.descartada;
        }
        trucoActivity.setResultadoRodada(numRodada, resultado);
        isRodadaPiscando = true;
        rodadaPiscaAte = System.currentTimeMillis() + 1200;
        notificaAnimacao(rodadaPiscaAte);
    }

    /**
     * Torna as cartas da mão de 10/11 visíveis e exibe a pergunta de aceite
     *
     * @param cartasParceiro cartas do seu parceiro
     */
    public void maoDeX(Carta[] cartasParceiro) {
        for (int i = 0; i <= 2; i++) {
            cartas[10 + i].copiaCarta(cartasParceiro[i]);
        }
        perguntaMaoDeX = "Aceita mão de " + trucoActivity.partida.getModo().pontuacaoParaMaoDeX() + "?";
        mostrarPerguntaMaoDeX = true;
    }

    /**
     * Faz com que o balão mostre uma frase por um tempo para um jogador.
     * <p>
     * As frases estão no strings.xml no formato balao_<chave>, e são arrays de
     * strings (das quais uma será selecionada para exibição).
     *
     * @param chave    diz o tipo de texto que aparece no balão. Ex.: "aumento_3"
     *                 para pedido de truco.
     * @param posicao  posição (1 a 4) do jogador que "dirá" a frase
     * @param tempoMS  tempo em que ela aparecerá (reduzido se a velocidade das animações for > 1)
     * @param rndFrase Número "grande" que identifica a frase do strings.xml dita
     *                 pelo jogador (índice_da_frase = rndFrase % frases.length())
     */
    public void diz(String chave, int posicao, int tempoMS, int rndFrase) {
        aguardaFimAnimacoes();
        mostraBalaoAte = System.currentTimeMillis() + tempoMS / Math.min(velocidade, 2);
        Resources res = getResources();
        String[] frasesBalao = res.getStringArray(res.getIdentifier("balao_" + chave, "array", "me.chester.minitruco"));
        int indiceFrase;
        do {
            indiceFrase = rndFrase % frasesBalao.length;
            rndFrase++;
        } while (frasesBalao.length > 1 && ultimaFrase.containsKey(chave) && ultimaFrase.get(chave) == indiceFrase);
        ultimaFrase.put(chave, indiceFrase);
        fraseBalao = frasesBalao[indiceFrase];
        posicaoBalao = posicao;
        notificaAnimacao(mostraBalaoAte);
    }

    /**
     * Verifica qual elemento foi tocado (ex.: uma das cartas do jogador ou um
     * dos botões) e executa a ação associada a ele.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (rectPergunta.contains(x, y)) {
                    ultimoyDaPergunta = y;
                }
                return true;
            case MotionEvent.ACTION_UP:
                ultimoyDaPergunta = -1;
                if (rectBotaoSim.contains(x, y)) {
                    respondePergunta(true);
                }
                if (rectBotaoNao.contains(x, y)) {
                    respondePergunta(false);
                }
                if (mostrarBotaoAumento && rectBotaoAumento.contains(x, y)) {
                    mostrarBotaoAumento = false;
                    statusVez = STATUS_VEZ_HUMANO_AGUARDANDO;
                    trucoActivity.partida.aumentaAposta(trucoActivity.jogadorHumano);
                }
                if (mostrarBotaoAbertaFechada && rectBotaoAbertaFechada.contains(x, y)) {
                    vaiJogarFechada = !vaiJogarFechada;
                }
                // Verificamos primeiro a carta mais à direita porque ela é desenhada
                // em cima da do meio, e esta em cima da carta à esquerda
                for (int i = 6; i >= 4; i--) {
                    if (cartas[i].isDentro(x, y)) {
                        jogaCarta(i - 4);
                    }
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (ultimoyDaPergunta > -1 && (mostrarPerguntaMaoDeX || mostrarPerguntaAumento)) {
                    int dy = y - ultimoyDaPergunta;
                    if (rectPergunta.top + dy < 0) {
                        dy = -rectPergunta.top;
                    } else if (rectPergunta.bottom + dy > getHeight()) {
                        dy = getHeight() - rectPergunta.bottom;
                    }
                    ultimoyDaPergunta = y;
                    rectPergunta.top = rectPergunta.top + dy;
                    rectPergunta.bottom = rectPergunta.bottom + dy;
                    rectBotaoSim.top = rectBotaoSim.top + dy;
                    rectBotaoSim.bottom = rectBotaoSim.bottom + dy;
                    rectBotaoNao.top = rectBotaoNao.top + dy;
                    rectBotaoNao.bottom = rectBotaoNao.bottom + dy;
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    /**
     * Encaminha resposta da pergunta em exibição (se houver uma) para a partida
     * em uma nova thread (para não travar a thread de UI) e oculta a caixa de
     * diálogo com a pergunta.
     *
     * @param resposta resposta do jogador (true=sim, false=não)
     */
    public void respondePergunta(boolean resposta) {
        new Thread(() -> {
            if (mostrarPerguntaAumento) {
                mostrarPerguntaAumento = false;
                trucoActivity.partida.respondeAumento(trucoActivity.jogadorHumano, resposta);
            } else if (mostrarPerguntaMaoDeX) {
                mostrarPerguntaMaoDeX = false;
                trucoActivity.partida.decideMaoDeX(trucoActivity.jogadorHumano, resposta);
            }
        }).start();
    }

    /**
     * Joga a carta na posição indicada, desde que seja a vez do jogador humano
     * e a carta não tenha ainda sido descartada.
     *
     * @param posicao posição da carta na mão do jogador (0 a 2)
     */
    public void jogaCarta(int posicao) {
        CartaVisual carta = cartas[posicao + 4];
        if (carta.descartada) return;
        if (statusVez != STATUS_VEZ_HUMANO_OK) return;

        statusVez = STATUS_VEZ_OUTRO;
        carta.setFechada(vaiJogarFechada);
        trucoActivity.partida.jogaCarta(trucoActivity.jogadorHumano, carta);
    }

    private long calcTempoAteFimAnimacaoMS() {
        return animandoAte - System.currentTimeMillis();
    }

    /**
     * Recebe a informação de que é a vez de alguém jogar.
     *
     * @param humano true se for a vez do humano, false se for a vez de outro jogador
     */
    public void vez(boolean humano) {
        aguardaFimAnimacoes();
        if (humano) {
            statusVez = STATUS_VEZ_HUMANO_OK;
        } else {
            statusVez = STATUS_VEZ_OUTRO;
        }
    }

    /**
     * Entrega as cartas iniciais na mão de cada jogador.
     */
    public void distribuiMao() {

        aguardaFimAnimacoes();

        // Distribui as cartas em círculo
        for (int i = 0; i <= 2; i++) {
            for (int j = 1; j <= 4; j++) {
                CartaVisual c = cartas[4 + i + 3 * (j - 1)];
                c.setFechada(true);
                entregaCarta(c, j, i);
            }
        }

        // Atribui o valor correto às cartas do jogador e exibe
        for (int i = 0; i <= 2; i++) {
            CartaVisual c = cartas[4 + i];
            if (isInEditMode()) {
                c.setLetra("A23".charAt(i));
                c.setNaipe(i);
            } else {
                c.copiaCarta(trucoActivity.jogadorHumano.getCartas()[i]);
            }
            c.setFechada(false);
        }

        // Abre o vira, se for manilha nova
        if (!isInEditMode() && !trucoActivity.partida.getModo().isManilhaVelha()) {
            cartas[0].copiaCarta(trucoActivity.partida.cartaDaMesa);
            cartas[0].visible = true;
        }

    }

    public void aceitouAumentoAposta() {
        if (statusVez == STATUS_VEZ_HUMANO_AGUARDANDO) {
            statusVez = STATUS_VEZ_HUMANO_OK;
        }
    }

    /**
     * Recolhe o vira e as cartas jogadas de volta para o baralho
     */
    public void recolheMao() {
        aguardaFimAnimacoes();
        cartas[0].visible = false;
        for (int i = 4; i <= 15; i++) {
            CartaVisual c = cartas[i];
            if ((c.top != topBaralho) || (c.left != leftBaralho)) {
                c.movePara(leftBaralho, topBaralho, 50);
                c.copiaCarta(null);
                c.descartada = false;
                c.escura = false;
                cartasJogadas.remove(c);
            }
        }
    }

    /**
     * Joga a carta no meio da mesa
     */
    public void descarta(Carta c, int posicao) {

        aguardaFimAnimacoes();

        // Coloca a carta no meio da tela, mas "puxando" na direção
        // de quem jogou
        int topFinal = calcPosTopDescartada(posicao);
        int leftFinal = calcPosLeftDescartada(posicao);

        // Pega uma carta visual naquela posição...
        CartaVisual cv = null;
        for (int i = 0; i <= 2; i++) {
            CartaVisual cvCandidata = cartas[i + 1 + posicao * 3];
            // ...que não tenha sido descartada...
            if (cvCandidata.descartada) {
                continue;
            }
            // ...e, no caso de um humano (ou parceiro em mão de 10/11), que
            // corresponda à carta da partida
            cv = cvCandidata;
            if (c.equals(cvCandidata)) {
                break;
            }
        }

        // Não deveria acontecer, mas como é só animação, podemos ignorar
        // se a carta não estiver na mão
        if (cv == null) {
            return;
        }

        // Executa a animação de descarte
        cv.copiaCarta(c);
        cv.movePara(leftFinal, topFinal, 200);
        cv.descartada = true;
        cartasJogadas.addElement(cv);

    }

    public void setVisivel(boolean visivel) {
        this.visivel = visivel;
    }

    /**
     * Exibe uma carta na posição apropriada, animando
     * <p>
     *
     * @param carta      Carta a distribuir
     * @param numJogador Posição do jogador, de 1 a 4 (1 = humano).
     * @param posicao    posição da carta na mão do jogador (0 a 2)
     */
    private void entregaCarta(CartaVisual carta, int numJogador, int posicao) {
        if (numJogador == 3 || numJogador == 4) {
            posicao = 2 - posicao;
        }
        carta.movePara(calcPosLeftCarta(numJogador, posicao), calcPosTopCarta(numJogador, posicao), 85);
    }

    /**
     * @return Coordenada x da i-ésima carta na mão do jogador em questão
     */
    private int calcPosLeftCarta(int numJogador, int i) {
        int deslocamentoHorizontalEntreCartas = CartaVisual.largura * 7 / 8;
        switch (numJogador) {
            case 1:
            case 3:
                return (getWidth() / 2) - (CartaVisual.largura / 2) + (i - 1) * deslocamentoHorizontalEntreCartas;
            case 2:
                return getWidth() - CartaVisual.largura;
            case 4:
            default:
                return 0;
        }
    }

    /**
     * @return Coordenada y da i-ésima carta na mão do jogador em questão
     */
    private int calcPosTopCarta(int numJogador, int i) {
        int deslocamentoVerticalEntreCartas = CartaVisual.altura / 12;
        switch (numJogador) {
            case 1:
                return getHeight() - CartaVisual.altura;
            case 2:
            case 4:
                return getHeight() / 2 - CartaVisual.altura / 2 - (i - 1) * deslocamentoVerticalEntreCartas;
            case 3:
            default:
                return 0;
        }
    }


    /**
     * @return Coordenada x de uma carta descartada pelo jogador, com uma
     * pequena perturbação
     */
    private int calcPosLeftDescartada(int numJogador) {
        int leftFinal;
        leftFinal = getWidth() / 2 - CartaVisual.largura / 2;
        if (numJogador == 2) {
            leftFinal += CartaVisual.largura;
        } else if (numJogador == 4) {
            leftFinal -= CartaVisual.largura;
        }
        leftFinal += System.currentTimeMillis() % 5 - 2;
        return leftFinal;
    }

    /**
     * @return Coordenada y de uma carta descartada pelo jogador, com uma
     * pequena perturbação
     */
    private int calcPosTopDescartada(int numJogador) {
        int topFinal;
        topFinal = getHeight() / 2 - CartaVisual.altura / 2;
        if (numJogador == 1) {
            topFinal += CartaVisual.altura / 2;
        } else if (numJogador == 3) {
            topFinal -= CartaVisual.altura / 2;
        }
        topFinal += System.currentTimeMillis() % 5 - 2;
        return topFinal;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Fundo verde
        canvas.drawRGB(27, 142, 60);

        // Desenha as cartas que já foram jogadas (se houverem),
        // na ordem em que foram jogadas
        for (int i = 0; i < cartasJogadas.size(); i++) {
            try {
                cartasJogadas.elementAt(i).draw(canvas);
            } catch (ArrayIndexOutOfBoundsException e) {
                // Não faz nada (a partida encerrou no meio de um refresh,
                // por isso a carta não está lá)
            }
        }

        // Desenha as cartas restantes, e o vira por cima de todas
        for (CartaVisual carta : cartas) {
            if (carta != null && !cartasJogadas.contains(carta) && carta != cartas[0]) {
                carta.draw(canvas);
            }
        }
        cartas[0].draw(canvas);

        // Desliga o destaque da carta que fez a rodada e escurece as cartas já
        // descartadas (para não confundir com as próximas)
        long agora = System.currentTimeMillis();
        if ((agora > rodadaPiscaAte) && isRodadaPiscando) {
            if (cartaQueFez != null) {
                cartaQueFez.destacada = false;
            }
            isRodadaPiscando = false;
        }

        // Caixa de diálogo (mão de 10/11 ou aumento)
        if (mostrarPerguntaMaoDeX || mostrarPerguntaAumento) {
            String textoPergunta;
            if (mostrarPerguntaAumento) {
                textoPergunta = perguntaAumento;
            } else {
                textoPergunta = perguntaMaoDeX;
            }
            paintPergunta.setAntiAlias(true);
            paintPergunta.setColor(Color.BLACK);
            paintPergunta.setStyle(Style.FILL);
            canvas.drawRect(rectPergunta, paintPergunta);
            paintPergunta.setColor(Color.WHITE);
            paintPergunta.setStyle(Style.STROKE);
            canvas.drawRect(rectPergunta, paintPergunta);

            paintPergunta.setTextSize(tamanhoFonte * 0.75f);
            paintPergunta.setTextAlign(Align.CENTER);
            paintPergunta.setStyle(Style.FILL);
            canvas.drawText(textoPergunta, rectPergunta.centerX(), rectPergunta.top + paintPergunta.getTextSize() * 2.3f, paintPergunta);

            int tamanhoIndicador = (int) (rectBotaoSim.height() / 2);
            int margemIndicador = tamanhoIndicador / 10;
            indicadorDrag.setBounds(rectPergunta.right - tamanhoIndicador, rectPergunta.top + margemIndicador, rectPergunta.right, rectPergunta.top + tamanhoIndicador);
            indicadorDrag.draw(canvas);

            desenhaBotao("Sim", canvas, rectBotaoSim);
            desenhaBotao("Não", canvas, rectBotaoNao);
        }

        desenhaBalao(canvas);
        desenhaIndicadorDeVez(canvas);
        if (mostrarBotaoAumento) {
            desenhaBotao(textosBotaoAumento.get(valorProximaAposta), canvas, rectBotaoAumento);
        }
        if (mostrarBotaoAbertaFechada) {
            desenhaBotao(vaiJogarFechada ? "Aberta" : "Fechada", canvas, rectBotaoAbertaFechada);
        }

        // TODO: modo automático nunca pede truco (e crasha bonito se a gente
        //  tenta pedir aqui; de qualquer forma, mover isso pra outro lugar)
        if (trucoActivity != null && trucoActivity.partida != null && trucoActivity.partida.isJogoAutomatico()) {
            jogaCarta(0);
            jogaCarta(1);
            jogaCarta(2);
            respondePergunta(rand.nextBoolean());
        }
    }

    private void desenhaBotao(String texto, Canvas canvas, RectF outerRect) {
        // TODO evitar instanciar objetos aqui (e no caller)
        Paint paint = new Paint();
        paint.setStyle(Style.FILL);
        paint.setAntiAlias(true);
        paint.setTextSize(tamanhoFonte * 0.75f);
        // Borda
        paint.setColor(Color.GRAY);
        canvas.drawRoundRect(outerRect, tamanhoFonte * 4 / 5, tamanhoFonte * 4 / 5, paint);
        // Interior
        paint.setColor(0xFF1D3929);
        RectF innerRect = new RectF(outerRect.left + 4, outerRect.top + 4, outerRect.right - 4, outerRect.bottom - 4);
        canvas.drawRoundRect(innerRect, tamanhoFonte * 4 / 5, tamanhoFonte * 4 / 5, paint);
        // Texto
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Align.CENTER);
        canvas.drawText(texto, outerRect.centerX(), outerRect.centerY() - tamanhoFonte * 0.2f + tamanhoFonte * 0.5f, paint);
    }

    private void desenhaIndicadorDeVez(Canvas canvas) {
        if (statusVez == STATUS_VEZ_HUMANO_AGUARDANDO) {
            return;
        }
        Paint paintSetaVez = new Paint();
        paintSetaVez.setColor(Color.YELLOW);
        paintSetaVez.setTextAlign(Align.CENTER);
        paintSetaVez.setTextSize(CartaVisual.altura / 3);
        switch (posicaoVez) {
            case 1:
                canvas.drawText("\u21E9", getWidth() / 2, getHeight() - CartaVisual.altura * 14 / 12, paintSetaVez);
                break;
            case 2:
                canvas.drawText("\u21E8", getWidth() - CartaVisual.largura * 15 / 12, getHeight() / 2, paintSetaVez);
                break;
            case 3:
                canvas.drawText("\u21E7", getWidth() / 2, CartaVisual.altura * 16 / 12, paintSetaVez);
                break;
            case 4:
                canvas.drawText("\u21E6", CartaVisual.largura * 15 / 12, getHeight() / 2, paintSetaVez);
                break;
        }
    }

    /**
     * Desenha a parte gráfica do balão (sem o texto). O nome é meio mentiroso,
     * porque também desenha a ponta. É chamada várias vezes para compor o
     * contorno, antes de estampar o texto
     *
     * @param canvas         onde ele será desenhado
     * @param x              esquerda
     * @param y              topo
     * @param largBalao      largura
     * @param altBalao       altura
     * @param quadrantePonta Quadrante (cartesiano) onde aparece a ponta do balão (com
     *                       relação a ele mesmo)
     */
    private void desenhaElipseBalao(Canvas canvas, int x, int y, int largBalao, int altBalao, int quadrantePonta, Paint paint) {
        // Elipse principal
        paint.setAntiAlias(true);
        canvas.drawArc(new RectF(x, y, x + largBalao - 1, y + altBalao - 1), 0, 360, false, paint);
        // Ponta (é um triângulo que desenhamos linha a linha)
        paint.setAntiAlias(false);
        int xi;
        for (int i = altBalao / 4; i < altBalao; i++) {
            if (quadrantePonta == 2 || quadrantePonta == 3) {
                xi = x + altBalao * 3 / 2 - i;
            } else {
                xi = x - altBalao * 3 / 2 + i + largBalao;
            }
            int sinaly = quadrantePonta < 3 ? -1 : 1;
            canvas.drawLine(xi, y + altBalao / 2, xi, y + altBalao / 2 + i * sinaly, paint);
        }
    }

    /**
     * Desenha o balão no lugar certo, se ele estiver visível
     *
     * @param canvas canvas onde ele será (ou não) desenhado.
     */
    private void desenhaBalao(Canvas canvas) {
        if (fraseBalao != null && mostraBalaoAte > System.currentTimeMillis()) {

            // Determina o tamanho e a posição do balão e o quadrante da
            // ponta
            final int MARGEM_BALAO_LEFT = (int) tamanhoFonte;
            final int MARGEM_BALAO_TOP = (int) tamanhoFonte / 2;
            Paint paintFonte = new Paint();
            paintFonte.setAntiAlias(true);
            paintFonte.setTextSize(tamanhoFonte);
            Rect bounds = new Rect();
            paintFonte.setColor(Color.BLACK);
            paintFonte.getTextBounds(fraseBalao, 0, fraseBalao.length(), bounds);

            int largBalao = bounds.width() + 2 * MARGEM_BALAO_LEFT;
            int altBalao = (int) (bounds.height() + 2.5 * MARGEM_BALAO_TOP);
            int x = 0, y = 0;
            int quadrantePonta = 0;
            switch (posicaoBalao) {
                case 1:
                    x = (canvas.getWidth() - largBalao) / 2 - CartaVisual.largura;
                    y = canvas.getHeight() - altBalao * 4 - 3;
                    quadrantePonta = 4;
                    break;
                case 2:
                    x = canvas.getWidth() - largBalao - 3;
                    y = (canvas.getHeight() - altBalao) / 2;
                    quadrantePonta = 1;
                    break;
                case 3:
                    x = (canvas.getWidth() - largBalao) / 2 + CartaVisual.largura;
                    y = 3 + altBalao / 2;
                    quadrantePonta = 2;
                    break;
                case 4:
                    x = 3;
                    y = (canvas.getHeight() - altBalao) / 2 - CartaVisual.altura;
                    quadrantePonta = 3;
                    break;
            }

            // O balão tem que ser branco, com uma borda preta. Como
            // ele só aparece em um refresh, vamos pela força bruta,
            // desenhando ele deslocado em torno da posição final em
            // preto e em seguida desenhando ele em branco na posição
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setColor(Color.BLACK);
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    desenhaElipseBalao(canvas, x + i, y + j, largBalao, altBalao, quadrantePonta, paint);
                }
            }
            paint.setColor(corFundoCartaBalao);
            desenhaElipseBalao(canvas, x, y, largBalao, altBalao, quadrantePonta, paint);

            // Finalmente, escreve o texto do balão
            paint.setAntiAlias(true);
            canvas.drawText(fraseBalao, x + MARGEM_BALAO_LEFT, y + altBalao - MARGEM_BALAO_TOP - 4 * density, paintFonte);

        } else {
            fraseBalao = null;
        }

    }

    public void setPosicaoVez(int posicaoVez) {
        this.posicaoVez = posicaoVez;
    }

    public void setCorFundoCartaBalao(int corFundoCartaBalao) {
        this.corFundoCartaBalao = corFundoCartaBalao;
    }

    /**
     * Faz o jogador na posição indicada pedir um aumento de aposta.
     * <p>
     * Se a posição for de um dos adversários do humano, mostra a pergunta.
     *
     * @param posicao  posição (1 a 4) do jogador que "dirá" a frase
     * @param valor    valor para o qual está querendo aumentar
     * @param rndFrase Número "grande" que identifica a frase do strings.xml dita
     *                 pelo jogador (índice_da_frase = rndFrase % frases.length())
     */
    public void pedeAumento(int posicao, int valor, int rndFrase) {
        String valorStr = trucoActivity.partida.nomeNoTruco(valor);
        diz("aumento_" + valorStr, posicao,
                1500 + 200 * (valor / 3), rndFrase);
        if (posicao == 2 || posicao == 4) {
            perguntaAumento = "Aceita " + valorStr + "?";
            mostrarPerguntaAumento = true;
        }
    }

    public void escondePergunta() {
        mostrarPerguntaAumento = false;
        mostrarPerguntaMaoDeX = false;
    }

    /**
     * Mostra o botão de aumento com o texto apropriado ("truco", "seis", etc.)
     * o
     * @param valorProximaAposta valor para o qual se está querendo aumentar
     */
    public void mostraBotaoAumento(int valorProximaAposta) {
        this.valorProximaAposta = valorProximaAposta;
        mostrarBotaoAumento = true;
    }

    /**
     * Mostra o botão que permite jogar uma carta aberta ou fechada.
     * <p>
     * Ele é mostrado no estado padrão (aberta) e, se o jogador clicar nele,
     * ele alterna entre aberta e fechada.
     */
    public void mostraBotaoAbertaFechada() {
        mostrarBotaoAbertaFechada = true;
        vaiJogarFechada = false;
    }

    public void escondeBotaoAumento() {
        mostrarBotaoAumento = false;
    }

    public void escondeBotaoAbertaFechada() {
        mostrarBotaoAbertaFechada = false;
    }

    /**
     * Configura a mesa para mostrar o texto especificado quando o próximo
     * aumento for do valor indicado. Ex.: "truco" para 3 (ou 4), "seis" para 6,
     * etc.
     */
    public void setTextoAumento(int valor, String texto) {
        textosBotaoAumento.put(valor, texto);
    }
}
