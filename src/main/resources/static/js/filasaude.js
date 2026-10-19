/*
 * Tema claro por padrão, escuro por escolha da pessoa. A escolha fica salva neste navegador.
 */
(function () {
    var raiz = document.documentElement;
    var CHAVE = 'filasaude-tema';

    function atualizarBotoes() {
        var escuro = raiz.getAttribute('data-tema') === 'escuro';
        document.querySelectorAll('[data-alternar-tema]').forEach(function (botao) {
            botao.setAttribute('aria-pressed', String(escuro));
            var rotulo = botao.querySelector('[data-rotulo-tema]');
            if (rotulo) {
                rotulo.textContent = escuro ? 'Tema claro' : 'Tema escuro';
            }
        });
    }

    document.addEventListener('click', function (evento) {
        if (!evento.target.closest('[data-alternar-tema]')) {
            return;
        }
        var escuro = raiz.getAttribute('data-tema') !== 'escuro';
        if (escuro) {
            raiz.setAttribute('data-tema', 'escuro');
        } else {
            raiz.removeAttribute('data-tema');
        }
        try {
            localStorage.setItem(CHAVE, escuro ? 'escuro' : 'claro');
        } catch (e) { /* sem armazenamento: vale só nesta página */ }
        atualizarBotoes();
    });

    atualizarBotoes();
})();

/*
 * Busca rápida por nome ou CPF: filtra as opções de um <select>.
 * Uso: <input data-filtrar="idDoSelect">
 */
(function () {
    var somenteDigitos = function (texto) { return texto.replace(/\D/g, ''); };
    var semAcento = function (texto) {
        return texto.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
    };
    document.querySelectorAll('[data-filtrar]').forEach(function (campo) {
        var lista = document.getElementById(campo.dataset.filtrar);
        if (!lista) {
            return;
        }
        campo.addEventListener('input', function () {
            var termo = semAcento(campo.value.trim());
            var digitos = somenteDigitos(termo);
            var primeiraVisivel = null;
            Array.prototype.forEach.call(lista.options, function (opcao) {
                if (!opcao.value) {
                    return; // mantém a opção "Selecione"
                }
                var texto = semAcento(opcao.text);
                var visivel = !termo || texto.indexOf(termo) >= 0
                        || (digitos.length > 0 && somenteDigitos(opcao.text).indexOf(digitos) >= 0);
                opcao.hidden = !visivel;
                if (visivel && !primeiraVisivel) {
                    primeiraVisivel = opcao;
                }
            });
            if (lista.selectedOptions.length && lista.selectedOptions[0].hidden) {
                lista.value = '';
            }
            if (termo && primeiraVisivel && !lista.value) {
                lista.value = primeiraVisivel.value;
            }
        });
    });
})();

/*
 * Confirmação de ações críticas (Manual da marca, seção 6):
 * janela com título, a consequência em uma frase e dois botões ("Sim, ..." e "Voltar").
 *
 * Uso: <form data-confirmar="Finalizar o atendimento de Maria Silva?"
 *            data-consequencia="O horário de término será registrado..."
 *            data-acao="Sim, finalizar">
 */
(function () {
    var dialogo = document.getElementById('confirmacao');
    if (!dialogo || typeof dialogo.showModal !== 'function') {
        return; // Sem suporte a <dialog>: o formulário é enviado normalmente
    }
    var titulo = dialogo.querySelector('[data-confirmacao-titulo]');
    var texto = dialogo.querySelector('[data-confirmacao-texto]');
    var botaoConfirmar = dialogo.querySelector('[data-confirmacao-sim]');
    var formularioPendente = null;

    document.addEventListener('submit', function (evento) {
        var formulario = evento.target;
        if (!formulario.dataset.confirmar) {
            return;
        }
        evento.preventDefault();
        formularioPendente = formulario;
        titulo.textContent = formulario.dataset.confirmar;
        texto.textContent = formulario.dataset.consequencia || '';
        texto.hidden = !formulario.dataset.consequencia;
        botaoConfirmar.textContent = formulario.dataset.acao || 'Confirmar';
        dialogo.showModal();
    });

    dialogo.addEventListener('close', function () {
        if (dialogo.returnValue === 'sim' && formularioPendente) {
            formularioPendente.submit();
        }
        formularioPendente = null;
        dialogo.returnValue = '';
    });
})();
