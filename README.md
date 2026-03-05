NextStep Mobile 🚀
NextStep Mobile é uma solução nativa desenvolvida para auxiliar Microempreendedores Individuais (MEIs) na gestão de seus negócios. O aplicativo transforma dados brutos em inteligência de negócio, permitindo o acompanhamento de transações financeiras e o monitoramento do limite de faturamento anual em tempo real.

📋 Funcionalidades Principais
Autenticação Segura: Sistema de login integrado ao Supabase Auth utilizando tokens JWT para persistência de sessão.

Dashboard Financeiro: Visualização clara de Saldo Total, Receitas e Despesas.

Monitoramento MEI: Barra de progresso dinâmica que alerta o usuário conforme ele se aproxima do limite de faturamento de R$ 81.000,00.

Histórico de Transações: Lista detalhada de entradas e saídas consumida via API REST.

Análise de Risco: Interface preparada para exibir status de risco e faturamento atual de empresas cadastradas.

🛡️ Segurança e Arquitetura
O projeto segue as melhores práticas de desenvolvimento seguro (DevSecOps):

Proteção de Credenciais: Utilização do arquivo local.properties (ignorado pelo VCS) e BuildConfig para garantir que chaves de API e URLs sensíveis nunca sejam expostas no código-fonte público.

Row Level Security (RLS): O banco de dados Supabase está protegido por políticas de RLS, garantindo que cada usuário acesse exclusivamente seus próprios dados financeiros.

Arquitetura REST: Consumo de APIs utilizando Retrofit 2 e OkHttp3, com interceptores para injeção automática de tokens de autorização.

🛠️ Tecnologias Utilizadas
Linguagem: Java.

Arquitetura: Android Nativo.

Rede: Retrofit 2.9.0 & OkHttp.

Serialização: Gson Converter.

Interface: Material Design, ConstraintLayout e RecyclerView.

Backend: Supabase (PostgreSQL, Auth e API REST).
