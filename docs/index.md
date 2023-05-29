# miniTruco - Documentação

EM CONSTRUÇÃO (finge que tem uma gif dos anos 90 de men-at-work aqui)

- [miniTruco - Documentação](#minitruco---documentação)
  - [Introdução](#introdução)
  - [Filosofia](#filosofia)
  - [Terminologia](#terminologia)
  - [Pré-requisitos e Configuração](#pré-requisitos-e-configuração)
  - [Organização](#organização)
  - [Modelo de Classes (single player)](#modelo-de-classes-single-player)
  - [Multiplayer](#multiplayer)
    - [Bluetooth](#bluetooth)
    - [Internet (em desenvolvimento)](#internet-em-desenvolvimento)
  - [Testes (ou falta de)](#testes-ou-falta-de)
  - [Estratégia dos bots](#estratégia-dos-bots)


## Introdução

## Filosofia

O objetivo principal é que o jogo possa ser acessível para o maior número de pessoas possível. Isso orienta algumas decisões de design e implementação, tais como:

- Foco na plataforma Android, que é a mais popular no Brasil
- Interface adaptável a diferentes resoluções, tamanho de tela e orientações
- Abrir o jogo a variações nas regras (atualmente isoladas como modos de jogo nos descendentes da classe [`Modo`](../core/src/main/java/me/chester/minitruco/core/Modo.java))
- Possibilidade de jogar offline (contra bots), via Bluetooth ou online
- Prioridade ao idioma português (tanto para a interface quanto para o código-fonte; vide seção "Estratégia dos bots") - pode parecer contraprodutivo, mas Truco é um fenômeno largamente brasileiro, e a maioria dos jogadores (e potenciais contribuidores) não fala inglês
- Uso de termos como "internet" ao invés de "online" (uma exceção foi usar "bot", por ser um termo popular e não ter um bom equivalente em português)

Outras escolhas têm razões históricas. Por exemplo, o jogo foi inicialmente desenvolvido para J2ME/MIDP, e depois portado para Android, antes da existência do Kotlin (e eu não pretendo migrar enquanto não me convencer do compromisso do Google com a linguagem).

A interface do jogo em si é uma `View` customizada ([`MesaView`](../app/src/main/java/me/chester/minitruco/android/MesaView.java)) e todo o resto usa `Layout`s tradicionais ao invés de Compose/Flutter/etc. para suportar versões mais antigas do Android; à medida em que os números dessas forem diminuindo ou zerando, isso pode mudar.

## Terminologia

O [vocabulário típico do truco](https://www.jogosdorei.com.br/blog/girias-do-truco/) é usado sempre que possível, mas alguns termos são necessários para evitar ambiguidades e consolidar os diferentes modos de jogo:

- **Aumento**: quando um jogador pede para aumentar o valor da rodada ("truco", que aumenta para 3 ou 4 pontos, "seis", "oito"/"nove" ou "doze"), conforme o modo de jogo.
- **Mão de X**: é a mão de 11 do truco paulista, ou mão de 10 do truco mineiro (quando apenas uma das duplas tem essa pontuação e pode optar por jogar ou não).

## Pré-requisitos e Configuração



## Organização

O projeto está dividido em três módulos Gradle:

- `core`: contém a lógica do jogo, independente de plataforma
- `app`: contém a implementação do aplicativo Android
- `server`: contém o servidor para jogo online (atualmente em desenvolvimento e com o acesso escondido no aplicativo).

## Modelo de Classes (single player)

## Multiplayer

### Bluetooth

### Internet (em desenvolvimento)

## Testes (ou falta de)

Quando este projeto começou, eu não tinha qualquer conhecimento da cultura de testes no desenvolvimento de software - isso só veio quando ele já estava portado para Android - e o ferramental para este ambiente (ou minha capacidade de utilizar ele) era um tanto limitado.

Por conta disso (e também tendo em vista a expansão para outras plataformas), o primeiro passo que eu tomei foi isolar o módulo `core` com as classes que representam a lógica do jogo, e que não dependem de nenhuma plataforma específica.

Em seguida, adicionei o JUnit 5 ao projeto, o que permitiu escrever um primeiro conjunto (bem tímido) de testes unitários em uma classe e tentar fazer um teste de integração do jogo todo rodando.

O próximo passo será melhorar isso e ampliar a cobertura de testes. Esse processo pode ser acompanhado em [#41](https://github.com/chesterbr/minitruco-android/issues/41) e será atualizado aqui.

## Estratégia dos bots

Por ter consciência da minha condição de ~~🦆~~ _ahem_... jogador sub-par de truco, desde o início o jogo foi pensado de forma a facilitar a implementação de múltiplas estratégias de bots.

Todas as estratégias presentes atualmente no jogo foram escritas por pessoas que não eu. Ao inicializar um `JogadorBot`, o jogo sorteia uma estratégia dentre as disponíveis e associa a ele. Elas são subclasses de [`Estrategia`](../core/src/main/java/me/chester/minitruco/core/Estrategia.java)) que implementam os métodos:

- `joga()`: chamada apenas na vez do bot, retorna a carta que o bot deve jogar, ou se ele quer pedir aumento.
- `aceitaTruco()`: chamada quando a dupla adversária do bot pede aumento, retorna se o bot aceita ou não.
- `aceitaMaoDeX()`: chamada quando a dupla do bot tem a mão de 10/11 (e a dupla adversária tem menos pontos), retorna se o bot quer jogar ou não.

Estas classes recebem como parâmetro um objeto [`SituacaoJogo`](../core/src/main/java/me/chester/minitruco/core/SituacaoJogo.java), que contém todas as informações necessárias para o bot tomar uma decisão. Este objeto é criado de forma que o bot não tenha acesso a nenhuma informação além daquela que um jogador naquela posição teria.

Para testar uma estratégia, você pode substituir a lista de estratégias disponíveis no [`Jogo`](../core/src/main/java/me/chester/minitruco/core/Jogo.java) por uma lista com apenas a estratégia que você quer testar. Você pode ativar a opção "Jogo Automático" para que o `JogadorHumano` jogue sozinho, e deixar o pau comer. Também pode escrever testes unitários (basta criar uma `SituacaoJogo` e passar para sua classe).





