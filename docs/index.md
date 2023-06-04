<!-- omit in toc -->

# miniTruco - Documentação para Desenvolvimento

EM CONSTRUÇÃO (finge que tem uma gif dos anos 90 de men-at-work aqui)

- [miniTruco - Documentação para Desenvolvimento](#minitruco---documentação-para-desenvolvimento)
  - [Introdução](#introdução)
  - [Contribuindo](#contribuindo)
  - [História, Objetivos e Design](#história-objetivos-e-design)
  - [Terminologia](#terminologia)
  - [Pré-requisitos e Configuração](#pré-requisitos-e-configuração)
  - [Organização](#organização)
  - [Arquitetura de Classes](#arquitetura-de-classes)
    - [Partidas e Jogadores](#partidas-e-jogadores)
      - [Jogo simples (single player)](#jogo-simples-single-player)
      - [Jogo via Bluetooth](#jogo-via-bluetooth)
    - [Internet (em desenvolvimento)](#internet-em-desenvolvimento)
  - [Testes (ou falta de)](#testes-ou-falta-de)
  - [Estratégia dos bots](#estratégia-dos-bots)
    - [Parágrafo que eu não sei onde vai](#parágrafo-que-eu-não-sei-onde-vai)

## Introdução

O miniTruco é um jogo de truco para celulares, tablets e outros dispositivos Android. É um projeto de software livre, desenvolvido por hobby no meu tempo pessoal (com valiosas [colaborações](../README.md#créditos) de outras pessoas).

Você pode usar e modificar o código como quiser, dentro dos [termos da licença](../LICENSE). O objetivo deste documento é ajudar qualquer pessoa interessada a configurar, entender e/ou aprimorar este código.

## Contribuindo

Fique à vontade para [criar uma issue](https://github.com/chesterbr/minitruco-android/issues) no GitHub se encontrar um problema de configuração, encontrar um _bug_, tiver uma sugestão ou quiser contribuir de qualquer forma.

[Pull requests](https://docs.github.com/pt/pull-requests) são bem-vindas, mas não há garantia de aceite (em particular devido à falta de testes automáticos, que me obriga a testar tudo muito cuidadosamente).

O código é bastante antigo (baseado na ainda mais antiga [versão Java ME](https://github.com/chesterbr/minitruco-j2me)), mas aos poucos estou tentando modernizar.

Uma área que sempre pode ser melhorada é a de estratégias (veja a seção "[Estratégia dos bots](#estratégia-dos-bots)"); correções de bugs específicos de celulares ajudam muito também, já que eu não tenho muitos à disposição.

Existem vários issues [abertos](https://github.com/chesterbr/minitruco-android/issues) ([bugs](https://github.com/chesterbr/minitruco-android/issues?q=is%3Aopen+is%3Aissue+label%3Abug), melhorias e débitos técnicos); se você for trabalhar em algum deles com intenção de fazer um pull request, sugiro comentar no issue - terei prazer em orientar e coordenar esforços, o que otimiza o seu tempo e evita dificuldades na hora de fazer o merge. Se você quiser trabalhar em algo que não está listado, sugiro abrir uma issue para discutir antes.

_(isso, claro, se você pretende contribuir com o código "oficial" e receber o devido crédito; no mais, a licença permite que você faça o que quiser com o código, desde que respeite os termos dela)_

## História, Objetivos e Design

O nome "miniTruco" é uma alusão ao fato de que a versão original rodava até em celulares com pouca memória (64K) e tela minúscula (através de um baralho desenhado [pixel](https://github.com/chesterbr/minitruco-j2me/blob/aabad635b34eee346cd7e12324f471c70ed16836/miniTruco/res/naipes.png) a [pixel](https://github.com/chesterbr/minitruco-j2me/blob/aabad635b34eee346cd7e12324f471c70ed16836/miniTruco/res/valores.png)), e embora a realidade de hoje seja outra, o nome ainda simboliza o compromisso de com o minimalismo e a inclusão.

Os **objetivos principais** do projeto são:

- Rodar até nos aparelhos mais modestos que ainda estejam em uso no Brasil (com base nas estatísticas da Play Store)
- Suportar o maior número viável de variantes locais (eu costumo dizer que truco pode ser usado como GPS, porque você anda um pouco e a regra muda)
- Promover a inclusão (que às vezes falta até no truco "de verdade", por exemplo quando a irreverência cruza o limite e vira preconceito)

Isto orienta algumas **decisões de design** e implementação, tais como:

- Foco na plataforma Android, que é a mais popular no Brasil
- Interface adaptável a diferentes resoluções, tamanho de tela e orientações
- Suportar o jogo local (contra bots), via Bluetooth (sem o uso de internet) ou online (este último em desenvolvimento)
- Prioridade ao idioma português (tanto na interface quanto no código-fonte)
- Uso de termos amigáveis para não-_gamers_, por exemplo, "internet" ao invés de "online", sempre que possível (exceções como "bot" e "Bluetooth" foram feitas por eu não ter encontrado tradução adequada)
- Evitar a presunção de gênero ou qualquer outra característica pessoal da pessoa que joga
- Não usar anúncios ou qualquer outro tipo de monetização, growth hack, promoção, coleta de dados, cadastro, parceria, mecânica de engajamento, clickbait, etc.

## Terminologia

O [vocabulário típico do truco](https://www.jogosdorei.com.br/blog/girias-do-truco/) é usado sempre que possível, mas alguns termos são necessários para evitar ambiguidades e consolidar os diferentes modos de jogo:

- **Aumento**: quando um jogador pede para aumentar o valor da rodada ("truco", que aumenta para 3 ou 4 pontos, "seis", "oito"/"nove" ou "doze", conforme o modo de jogo).
- **Mão de X**: é a mão de 11 do truco paulista, ou mão de 10 do truco mineiro (quando apenas uma das duplas tem essa pontuação e pode optar por jogar ou não).

## Pré-requisitos e Configuração

O jogo foi [inicialmente](https://github.com/chesterbr/minitruco-j2me) desenvolvido para [Java ME](https://en.wikipedia.org/wiki/Java_Platform,_Micro_Edition), e depois portado para Android. Isso aconteceu antes da existência do [Kotlin](https://kotlinlang.org/), portanto o código é baseado em [Java](https://www.java.com/pt-BR/) (e eu não pretendo migrar enquanto não me convencer do [compromisso do Google](https://killedbygoogle.com/) com a linguagem).

O projeto usa o [Gradle](https://gradle.org/) para gerenciamento de dependências e build. A IDE usada atualmente é o [Android Studio](https://developer.android.com/studio), mas pode ser importado em outras IDEs que suportem Gradle.

Em princípio, basta abrir o projeto no Android Studio e toda a configuração deve acontecer automaticamente, permitindo executar em dispositivos virtuais ou físicos.

Eu recomendo testar em dispositivos físicos mesmo, em particular se for usar Bluetooth (o emulador do Android Studio até simula Bluetooth, mas mas só em versões recentes do Android, e limitado a dois dispositivos), mas é totalmente possível desenvolver sem um.

Comentários, variáveis e afins usam o português do Brasil, o mesmo valendo para mensagens de commit. Por conta disso, é recomendado desligar, no Android Studio a checagem de ortografia (`Preferences` => `Editor` => `Inspections` => `Proofreading` => `Typo`), pois ela assume inglês.

As convenções de código estão no arquivo [`.editorConfig`](../.editorConfig), e o Android Studio deve adotar elas automaticamente. Sim, você vai encontrar código que não adere a elas ainda; eu estou tentando melhorar isso aos poucos.

## Organização

O projeto está dividido em três módulos Gradle:

- `core`: contém a lógica do jogo, independente de plataforma
- `app`: contém a implementação do aplicativo Android
- `server`: contém o servidor para jogo online (atualmente em desenvolvimento e com o acesso escondido no aplicativo).

## Arquitetura de Classes

### Partidas e Jogadores

Uma partida pode envolver jogadores humanos e/ou bots, e estes jogadores podem estar todos no mesmo dispositivo (single player) ou em dispositivos diferentes (multiplayer). Isso motivou a criação de duas classes abstratas:

- `Partida` É responsável por coordenar as ações dos jogadores ao longo de uma partida de truco.
- `Jogador` representa um jogador, e é responsável por interagir com o usuário (humano) ou com a estratégia (bot) e com a `Partida`.

```mermaid
classDiagram
direction TB
    Partida <|-- PartidaLocal
    Partida <|-- PartidaRemota
    Jogador <|-- JogadorHumano
    Jogador <|-- JogadorBot
    Jogador <|-- JogadorBluetooth
    Jogador <|-- JogadorDummy
```

Diferentes implementações são combinadas para suportar diferentes modos de jogo.

#### Jogo simples (single player)

```mermaid
classDiagram
direction LR
    PartidaLocal -- "1" JogadorHumano
    PartidaLocal -- "3" JogadorBot
    JogadorBot -- Estrategia
```

Neste modo (que é o padrão do jogo, iniciado ao tocar o botão "Jogar) as três classes mais fundamentais são usadas:

- `PartidaLocal` mantém o estado do jogo (pontos, cartas jogadas, etc) e coordena as ações dos `Jogador`es, chamando métodos deles (_notificações_) sempre que algo acontece no jogo (ex.: início de rodada, alguém jogou uma carta, pediu aumento de truco, etc.) e esperando que eles respondam com _comandos_ (ex.: jogar uma carta, aceitar o truco, etc.) da mesma forma. Ela é conectada a um `JogadorHumano` e três `JogadorBot`.
- `JogadorHumano` faz a ponte entre a partida e a UI do Android. Ele recebe as notificações da partida e traduz em elementos visuais (de `TrucoActivity` e `MesaView`). Quando o usuário interage com estes elementos, ela envia os comandos correspondentes à partida.
- `JogadorBot` faz a ponte entre a partida e uma `Estrategia`. Da mesma forma que `JogadorHumano`, ela recebe as notificações da partida, mas se concentra basicamente em eventos que precisam de uma resposta (ex.: é a vez daquele bot), chamando métodos de `Estrategia` e, de acordo com a resposta, enviando comandos à partida.

Vale observar que a UI só reage quando a partida notifica `JogadorHumano`. Por exemplo, se ele pede truco, o balão só aparece quando a partida manda a notificação dizendo "jogador X pediu truco". Isso também vale para eventos dos outros jogadores: quando um bot joga uma carta, a animação aparece quando `JogadorHumano` recebe a notfiifição de "jogador Y jogou a carta Ij".

Essa separação radical simplifica os jogadores (`JogadorHumano` não precisa entender as regras do jogo, `JogadorBot` só se preocupa em jogar), evita trapaças (`PartidaLocal` é a única autoridade) e permite total reuso no multiplayer, como veremos a seguir.

#### Jogo via Bluetooth

Para jogar via Bluetooth, um aparelho seleciona a opção "Criar Jogo", que abre uma `ServidorBluetoothActivity`. Esta aguarda por conexões de outros aparelhos, e quando um se conecta, ela cria um `JogadorBluetooth`.

`JogadorBluetooth` recebe notificações da `PartidaLocal` da mesma forma que `JogadorHumano`, mas em vez de traduzir para a UI, ela traduz em comandos textuais, que são enviados ao outro aparelho via Bluetooth. Da mesma forma, ela recebe notificações textuais do outro aparelho e traduz em comandos para a partida.

```mermaid
classDiagram
direction LR
    PartidaLocal -- "1" JogadorHumano
    PartidaLocal -- "3" JogadorBluetooth
    JogadorBluetooth -- ServidorBluetoothActivity
    note for ServidorBluetoothActivity "conversa com cliente\nvia Bluetooth"
```

De forma análoga, o aparelho que seleciona a opção "Procurar Jogo" abre uma `ClienteBluetoothActivity`, que se conecta no aparelho servidor. Aqui quem faz a tradução de notificações e comandos para o protocolo textual é `PartidaRemota`:

```mermaid
classDiagram
    PartidaRemota -- ClienteBluetoothActivity
    PartidaRemota -- "1" JogadorHumano
    note for ClienteBluetoothActivity "conversa com servidor\nvia Bluetooth"
```

Parece complicado, mas a grande vantagem é que nem `PartidaLocal` (no servidor), nem `JogadorHumano` (no cliente) precisam saber que estão conversando via Bluetooth, graças aos _proxies_ `JogadorBluetooth` e `PartidaRemota`. Isso permite que o mesmo código seja usado para jogar localmente ou via Bluetooth, e também permite que o jogo seja jogado via Bluetooth ou internet.

### Internet (em desenvolvimento)

TODO
## Testes (ou falta de)

Quando este projeto começou, eu não tinha qualquer conhecimento da cultura de testes no desenvolvimento de software - isso só veio quando ele já estava portado para Android - e o ferramental para este ambiente (ou minha capacidade de utilizar ele) era um tanto limitado.

Por conta disso (e também tendo em vista a expansão para outras plataformas), o primeiro passo que eu tomei foi isolar o módulo `core` com as classes que representam a lógica do jogo, e que não dependem de nenhuma plataforma específica.

Em seguida, adicionei o [JUnit 5](https://junit.org/junit5/) e um primeiro conjunto de testes unitários (mais uma tentativa de teste de integração) no módulo `core`.

O próximo passo será melhorar isso e ampliar a cobertura de testes. Esse processo pode ser acompanhado em [#41](https://github.com/chesterbr/minitruco-android/issues/41) e será atualizado aqui.

## Estratégia dos bots

Por ter consciência da minha condição de... ~~🦆~~ _aham_... jogador sub-ótimo de truco, desde o início o jogo foi pensado de forma a facilitar a implementação de estratégias de bots por gente mais capacitada.

Ao inicializar um `JogadorBot`, o jogo associa a ele uma das estratégias disponíveis, escolhida aleatoriamente. Elas são subclasses de [`Estrategia`](../core/src/main/java/me/chester/minitruco/core/Estrategia.java)) que implementam os métodos:

- `joga()`: chamada apenas na vez do bot, retorna a carta que o bot deveria jogar, ou se ele deveria pedir aumento.
- `aceitaTruco()`: chamada quando a dupla adversária do bot pede aumento, retorna se o bot deveria aceitar ou não.
- `aceitaMaoDeX()`: chamada quando a dupla do bot tem a mão de 10/11 (e a dupla adversária tem menos pontos), retorna se o bot deveria jogar ou não.

Estas classes recebem como parâmetro um objeto [`SituacaoJogo`](../core/src/main/java/me/chester/minitruco/core/SituacaoJogo.java), que contém todas as informações necessárias para o bot tomar uma decisão. Este objeto é criado de forma que a estratégia não tenha acesso a nenhuma informação além do que um jogador naquela posição saberia.

É importante observar alguns pontos:

- O bot não vai _necessariamente_ acatar a decisão da estratégia. Por exemplo, num jogo single-player com a opção "Humano decide" ativada, um aceite do truco ou mão de 11 vai apenas notificar o jogador (com uma frase como "Vamos nessa!"), mas não vai aumentar o valor da rodada ou iniciar a mão de 11.

- A `Estrategia` tem o mesmo tempo de vida do `JogadorBot` (ela é criada e destruída com o `Jogo`). Isso permite que ela mantenha um estado interno, se necessário, mas levando em conta o item acima, o mais seguro é não manter estado algum, e sim tomar toda a decisão baseado no `SituacaoJogo` recebido por cada método.

- A classe [`Carta`](../core/src/main/java/me/chester/minitruco/core/Carta.java) (que vai aparecer em propriedades de `SituacaoJogo` tais como `cartasJogadas` e `cartasJogador`) possui um método [`getValorTruco()`](../core/src/main/java/me/chester/minitruco/core/Carta.java#L163) que retorna o valor relativo daquela carta, levando em conta a manilha e o modo de jogo. A estratégia deve sempre usar esse valor (e não a letra/naipe da carta) para tomar a decisão.

Para testar uma estratégia, você pode substituir a lista de estratégias disponíveis no [`Jogo`](../core/src/main/java/me/chester/minitruco/core/Jogo.java) por uma lista com apenas a estratégia que você quer testar. Você pode ativar a opção "Jogo Automático" para que o `JogadorHumano` jogue sozinho, e deixar o pau comer. Também pode escrever testes unitários (basta criar uma `SituacaoJogo` e passar para sua classe). Eu gostaria de no futuro ter maneiras melhores de testar uma estratégia (ex.: um modo que colocasse elas umas contra as outras).

### Parágrafo que eu não sei onde vai

A interface do jogo em si é uma `View` customizada ([`MesaView`](../app/src/main/java/me/chester/minitruco/android/MesaView.java)) e todo o resto usa `Layout`s tradicionais ao invés de Compose/Flutter/etc. para suportar versões mais antigas do Android; à medida em que os números dessas forem diminuindo ou zerando, isso pode mudar.
