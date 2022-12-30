miniTruco Android
-----------------

O miniTruco é um jogo de truco para celulares, tablets e outros dispositivos móvieis. O código original original foi escrito para a plataforma Java ME. Esta versão Android reutiliza parte daquele código (em particular as estratégias de jogo), mas muito foi reescrito e redesenhado.

O jogo ainda está em desenvolvimento e pode apresentar instabilidades. Reports de exceções e feedback (positivo ou negativo, desde que seja educado) através do Android Market são bem-vindos.

Este software é livre, e você é encorajado a usar e modificar seu código como quiser, dentro dos termos da licença abaixo (vide abaixo para maiores informações).

[1] A versão Java ME continua hospedada em http://code.google.com/p/minitruco


Requisitos do Código-Fonte
--------------------------

O código tem como plataforma-alvo o Android 2.2. Para a compilação é necessário apenas o Android Studio. Neste caso, basta importar a sub-pasta minitruco-android como projeto e testar diretamente no emulador.


Backlog
-------

Features/bugs:
- Nenhum! \o/

Débito Técnico:
- Refactor: Trocar os loops de thread baseados em sleep por um esquema wait/notify (JogadorCPU e JogoLocal). Ou pelo menos fazer algo melhor com as InterruptedException (vide http://bit.ly/172PUX)
- Mover strings (ex.: frases do botão de truco) para strings.xml
- Criar constantes, polimorfismos ou qualquer coisa para matar os magic numbers herdados do miniTruco Java ME. Ex.: posições dos jogadores, nomes das equipes.
- Criar uma documentação do desenvolvedor nos moldes de http://minitruco.googlecode.com/svn/tags/v3.12.00/docs/index.html

Para um histórico de versões, verifique as tags ou as atualizações na Play Store

Créditos e Licença
------------------

Copyright © 2005-2022 Carlos Duarte do Nascimento (Chester) - cd@pobox.com

Imagens de carta, fundo e logotipo: Copyright © 2010-2011 Vanessa Sabino (Bani)

Estratégias da CPU: Copyright © 2006-2011 Leonardo Sellani, Sandro Gasparotto

Estratégia da CPU (antiga): Copyright © 2006-2011 Willian Gigliotti

Seleção manual de servidor Bluetooth: Copyright © 2016 Rodolfo Vasconcelos

Tento mineiro: Copyright © 2011-2011 Guilherme Caram


Código-fonte: http://github.com/chesterbr/minitruco-android

A redistribuição o uso nas formas binária e código fonte, com ou sem
modificações, são permitidos contanto que as condições abaixo sejam
cumpridas:

- Redistribuições do código fonte devem conter o aviso de direitos
  autorais acima, esta lista de condições e o aviso de isenção de
  garantias subsequente.

- Redistribuições na forma binária devem reproduzir o aviso de direitos
  autorais acima, esta lista de condições e o aviso de isenção de
  garantias subsequente na documentação e/ou materiais fornecidos com
  a distribuição.

- Nem o nome do Chester, nem o nome dos contribuidores podem ser
  utilizados para endossar ou promover produtos derivados deste
  software sem autorização prévia específica por escrito.

ESTE SOFTWARE É FORNECIDO PELOS DETENTORES DE DIREITOS AUTORAIS E
CONTRIBUIDORES "COMO ESTÁ", ISENTO DE GARANTIAS EXPRESSAS OU TÁCITAS,
INCLUINDO, SEM LIMITAÇÃO, QUAISQUER GARANTIAS IMPLÍCITAS DE
COMERCIABILIDADE OU DE ADEQUAÇÃO A FINALIDADES ESPECÍFICAS. EM NENHUMA
HIPÓTESE OS TITULARES DE DIREITOS AUTORAIS E CONTRIBUIDORES SERÃO
RESPONSÁVEIS POR QUAISQUER DANOS, DIRETOS, INDIRETOS, INCIDENTAIS,
ESPECIAIS, EXEMPLARES OU CONSEQUENTES, (INCLUINDO, SEM LIMITAÇÃO,
FORNECIMENTO DE BENS OU SERVIÇOS SUBSTITUTOS, PERDA DE USO OU DADOS,
LUCROS CESSANTES, OU INTERRUPÇÃO DE ATIVIDADES), CAUSADOS POR QUAISQUER
MOTIVOS E SOB QUALQUER TEORIA DE RESPONSABILIDADE, SEJA RESPONSABILIDADE
CONTRATUAL, RESTRITA, ILÍCITO CIVIL, OU QUALQUER OUTRA, COMO DECORRÊNCIA
DE USO DESTE SOFTWARE, MESMO QUE HOUVESSEM SIDO AVISADOS DA
POSSIBILIDADE DE TAIS DANOS.
