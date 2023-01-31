
## Inicialização do Banco de Dados

Ao subir um container do zero o mesmo vai carregar todos os .sql e executa-los por ordem alfabetica / númerica.

Então se estiver subindo o banco pela primeira vez via container e o VOLUME ainda não existir então o banco vai ser criado automativamente com todos os scripts que estiverem
na pasta _2.x_

Uma vez que o banco já estiver no ar, ou seja, o VOLUME já foi criado, então todos as alterações deve ser aplicadas manualmente.

**IMPORTANTE**

Manter sempre os DBVersions prontos para que em um novo ambiente o mesmo funcione, ou seja, manter a nomecatura _DBVersion-X.X.X.sql_


