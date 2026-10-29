/* ============================================================
   FilaSaúde — JavaScript Global (Tema, Acessibilidade, Máscaras, Busca e Atendimento)
   ============================================================ */

(function () {
    var root = document.documentElement;

    /* ==================== Gerenciamento de Tema ==================== */
    function setTheme(theme) {
        theme = (theme === 'dark' || theme === 'escuro') ? 'dark' : 'light';
        root.setAttribute('data-theme', theme);
        root.dataset.theme = theme;
        root.setAttribute('data-tema', theme === 'dark' ? 'escuro' : 'claro');
        root.style.colorScheme = theme;

        try {
            localStorage.setItem('fs-tema-manual', theme);
            localStorage.setItem('filasaude-tema', theme === 'dark' ? 'escuro' : 'claro');
        } catch (e) {}

        var btnLight = document.getElementById('btn-theme-light');
        var btnDark = document.getElementById('btn-theme-dark');
        if (btnLight) {
            btnLight.classList.toggle('is-active', theme === 'light');
            btnLight.setAttribute('aria-pressed', String(theme === 'light'));
        }
        if (btnDark) {
            btnDark.classList.toggle('is-active', theme === 'dark');
            btnDark.setAttribute('aria-pressed', String(theme === 'dark'));
        }

        document.querySelectorAll('[data-alternar-tema]').forEach(function (btn) {
            btn.setAttribute('aria-pressed', String(theme === 'dark'));
            var rotulo = btn.querySelector('[data-rotulo-tema]');
            if (rotulo) {
                rotulo.textContent = (theme === 'dark') ? 'Tema claro' : 'Tema escuro';
            }
        });
    }

    document.addEventListener('click', function (e) {
        if (e.target.closest('[data-alternar-tema]')) {
            var isDark = root.getAttribute('data-theme') === 'dark' || root.getAttribute('data-tema') === 'escuro';
            setTheme(isDark ? 'light' : 'dark');
        }
    });

    /* ==================== Acessibilidade Global ==================== */
    function setFontScale(scale) {
        if (scale !== 'padrao' && scale !== 'medio' && scale !== 'grande') {
            scale = 'padrao';
        }
        root.setAttribute('data-font-scale', scale);
        try { localStorage.setItem('fs-a11y-font', scale); } catch (e) {}

        var btnReset = document.getElementById('btn-font-reset');
        if (btnReset) {
            btnReset.textContent = (scale === 'padrao') ? '100%' : (scale === 'medio' ? '112%' : '125%');
        }
    }

    function setContrast(ativo) {
        root.setAttribute('data-high-contrast', String(ativo));
        try { localStorage.setItem('fs-a11y-contrast', String(ativo)); } catch (e) {}
        var btn = document.getElementById('btn-toggle-contrast');
        if (btn) {
            btn.textContent = ativo ? 'Desativar' : 'Ativar';
            btn.classList.toggle('is-active', ativo);
        }
    }

    function setFocusEnhanced(ativo) {
        root.setAttribute('data-focus-enhanced', String(ativo));
        try { localStorage.setItem('fs-a11y-focus', String(ativo)); } catch (e) {}
        var btn = document.getElementById('btn-toggle-focus');
        if (btn) {
            btn.textContent = ativo ? 'Desativar' : 'Ativar';
            btn.classList.toggle('is-active', ativo);
        }
    }

    function setMotionReduced(ativo) {
        root.setAttribute('data-motion-reduced', String(ativo));
        try { localStorage.setItem('fs-a11y-motion', String(ativo)); } catch (e) {}
        var btn = document.getElementById('btn-toggle-motion');
        if (btn) {
            btn.textContent = ativo ? 'Desativar' : 'Ativar';
            btn.classList.toggle('is-active', ativo);
        }
    }

    function setDyslexia(ativo) {
        root.setAttribute('data-dyslexia', String(ativo));
        try { localStorage.setItem('fs-a11y-dyslexia', String(ativo)); } catch (e) {}
        var btn = document.getElementById('btn-toggle-dyslexia');
        if (btn) {
            btn.textContent = ativo ? 'Desativar' : 'Ativar';
            btn.classList.toggle('is-active', ativo);
        }
    }

    function initA11y() {
        var btnThemeLight = document.getElementById('btn-theme-light');
        var btnThemeDark = document.getElementById('btn-theme-dark');
        var btnFontDec = document.getElementById('btn-font-dec');
        var btnFontReset = document.getElementById('btn-font-reset');
        var btnFontInc = document.getElementById('btn-font-inc');
        var btnToggleContrast = document.getElementById('btn-toggle-contrast');
        var btnToggleFocus = document.getElementById('btn-toggle-focus');
        var btnToggleMotion = document.getElementById('btn-toggle-motion');
        var btnToggleDyslexia = document.getElementById('btn-toggle-dyslexia');
        var btnToggleVLibras = document.getElementById('btn-toggle-vlibras');

        if (btnThemeLight) btnThemeLight.addEventListener('click', function () { setTheme('light'); });
        if (btnThemeDark) btnThemeDark.addEventListener('click', function () { setTheme('dark'); });

        if (btnFontDec) btnFontDec.addEventListener('click', function () {
            var current = root.getAttribute('data-font-scale') || 'padrao';
            if (current === 'grande') setFontScale('medio');
            else setFontScale('padrao');
        });
        if (btnFontReset) btnFontReset.addEventListener('click', function () {
            setFontScale('padrao');
        });
        if (btnFontInc) btnFontInc.addEventListener('click', function () {
            var current = root.getAttribute('data-font-scale') || 'padrao';
            if (current === 'padrao') setFontScale('medio');
            else if (current === 'medio') setFontScale('grande');
        });

        if (btnToggleContrast) btnToggleContrast.addEventListener('click', function () {
            setContrast(root.getAttribute('data-high-contrast') !== 'true');
        });
        if (btnToggleFocus) btnToggleFocus.addEventListener('click', function () {
            setFocusEnhanced(root.getAttribute('data-focus-enhanced') !== 'true');
        });
        if (btnToggleMotion) btnToggleMotion.addEventListener('click', function () {
            setMotionReduced(root.getAttribute('data-motion-reduced') !== 'true');
        });
        if (btnToggleDyslexia) btnToggleDyslexia.addEventListener('click', function () {
            setDyslexia(root.getAttribute('data-dyslexia') !== 'true');
        });

        if (btnToggleVLibras) {
            btnToggleVLibras.addEventListener('click', function () {
                var vBtn = document.querySelector('[vw-access-button]');
                if (vBtn) vBtn.click();
            });
        }

        // Atalho Alt + A abre a página de acessibilidade
        document.addEventListener('keydown', function (e) {
            if (e.altKey && (e.key === 'a' || e.key === 'A')) {
                e.preventDefault();
                if (window.location.pathname !== '/acessibilidade') {
                    window.location.href = '/acessibilidade';
                } else {
                    var tituloA11y = document.querySelector('.page-title');
                    if (tituloA11y) tituloA11y.focus();
                }
            }
        });

        // Carregar estado salvo
        var savedManualTheme = localStorage.getItem('fs-tema-manual');
        var savedLegacyTheme = localStorage.getItem('filasaude-tema');
        var savedTheme = savedManualTheme === 'dark' || (savedManualTheme !== 'light' && savedLegacyTheme === 'escuro') ? 'dark' : 'light';
        setTheme(savedTheme);

        var fonteInicial = document.querySelector('input[name="tamanhoFonte"]:checked');
        var savedFont = localStorage.getItem('fs-a11y-font') || (fonteInicial ? fonteInicial.value : 'padrao');
        setFontScale(savedFont);

        var contrasteInicial = document.getElementById('altoContraste');
        var savedContrast = localStorage.getItem('fs-a11y-contrast') === null
            ? Boolean(contrasteInicial && contrasteInicial.checked)
            : localStorage.getItem('fs-a11y-contrast') === 'true';
        setContrast(savedContrast);
        var contrasteInput = contrasteInicial;
        if (contrasteInput) contrasteInput.checked = savedContrast;

        var focoInicial = document.getElementById('focoReforcado');
        var savedFocus = localStorage.getItem('fs-a11y-focus') === null
            ? Boolean(focoInicial && focoInicial.checked)
            : localStorage.getItem('fs-a11y-focus') === 'true';
        setFocusEnhanced(savedFocus);
        var focoInput = focoInicial;
        if (focoInput) focoInput.checked = savedFocus;

        var movimentoInicial = document.getElementById('reducaoMovimento');
        var savedMotion = localStorage.getItem('fs-a11y-motion') === null
            ? Boolean(movimentoInicial && movimentoInicial.checked)
            : localStorage.getItem('fs-a11y-motion') === 'true';
        if (localStorage.getItem('fs-a11y-motion') === null && window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
            savedMotion = true;
        }
        setMotionReduced(savedMotion);
        var movimentoInput = movimentoInicial;
        if (movimentoInput) movimentoInput.checked = savedMotion;

        var espacamentoInicial = document.getElementById('legibilidadeAprimorada');
        var savedDyslexia = localStorage.getItem('fs-a11y-dyslexia') === null
            ? Boolean(espacamentoInicial && espacamentoInicial.checked)
            : localStorage.getItem('fs-a11y-dyslexia') === 'true';
        setDyslexia(savedDyslexia);
        var espacamentoInput = espacamentoInicial;
        if (espacamentoInput) espacamentoInput.checked = savedDyslexia;

        document.querySelectorAll('input[name="tamanhoFonte"]').forEach(function (input) {
            input.checked = input.value === savedFont;
            input.addEventListener('change', function () { setFontScale(input.value); });
        });
        if (contrasteInput) contrasteInput.addEventListener('change', function () { setContrast(contrasteInput.checked); });
        if (focoInput) focoInput.addEventListener('change', function () { setFocusEnhanced(focoInput.checked); });
        if (movimentoInput) movimentoInput.addEventListener('change', function () { setMotionReduced(movimentoInput.checked); });
        if (espacamentoInput) espacamentoInput.addEventListener('change', function () { setDyslexia(espacamentoInput.checked); });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initA11y);
    } else {
        initA11y();
    }
})();

/* ============================================================
   Máscaras e Bloqueio de Caracteres (Telefone, CPF, Cartão SUS)
   ============================================================ */
(function () {
    function isTeclaNavegacao(e) {
        return e.key === 'Backspace' || e.key === 'Delete' || e.key === 'Tab' || e.key === 'Enter' ||
            e.key === 'Escape' || e.key === 'ArrowLeft' || e.key === 'ArrowRight' ||
            e.key === 'ArrowUp' || e.key === 'ArrowDown' || e.key === 'Home' || e.key === 'End' ||
            e.ctrlKey || e.metaKey || e.altKey;
    }

    function bloquearNaoDigitos(e) {
        if (isTeclaNavegacao(e)) return;
        if (!/^\d$/.test(e.key)) {
            e.preventDefault();
        }
    }

    /* Padrão exigido: (00) 9 8888-8888 (celular 11 dígitos) ou (00) 8888-8888 (fixo 10 dígitos) */
    function formatarTelefone(valor, isDelete) {
        var digitos = (valor || '').replace(/\D/g, '').slice(0, 11);
        if (!digitos) return '';
        if (digitos.length <= 2) {
            return isDelete ? '(' + digitos : (digitos.length === 2 ? '(' + digitos + ') ' : '(' + digitos);
        }
        // Quando o 3º dígito é 9 (celular) ou já tem 11 dígitos, formata como (00) 9 8888-8888
        var formatoCelular = (digitos.charAt(2) === '9' || digitos.length === 11);
        if (formatoCelular) {
            if (digitos.length === 3) {
                return '(' + digitos.slice(0, 2) + ') ' + digitos.slice(2, 3) + (isDelete ? '' : ' ');
            }
            if (digitos.length <= 7) {
                return '(' + digitos.slice(0, 2) + ') ' + digitos.slice(2, 3) + ' ' + digitos.slice(3);
            }
            return '(' + digitos.slice(0, 2) + ') ' + digitos.slice(2, 3) + ' ' + digitos.slice(3, 7) + '-' + digitos.slice(7, 11);
        }
        // Telefone fixo (10 dígitos): (00) 8888-8888
        if (digitos.length <= 6) {
            return '(' + digitos.slice(0, 2) + ') ' + digitos.slice(2);
        }
        return '(' + digitos.slice(0, 2) + ') ' + digitos.slice(2, 6) + '-' + digitos.slice(6, 10);
    }

    function formatarCpf(valor) {
        var digitos = (valor || '').replace(/\D/g, '').slice(0, 11);
        if (!digitos) return '';
        if (digitos.length <= 3) return digitos;
        if (digitos.length <= 6) return digitos.slice(0, 3) + '.' + digitos.slice(3);
        if (digitos.length <= 9) return digitos.slice(0, 3) + '.' + digitos.slice(3, 6) + '.' + digitos.slice(6);
        return digitos.slice(0, 3) + '.' + digitos.slice(3, 6) + '.' + digitos.slice(6, 9) + '-' + digitos.slice(9, 11);
    }

    function aplicarMascaraTelefone(campo) {
        if (!campo || campo.dataset.mascaraAplicada === 'true') return;
        campo.dataset.mascaraAplicada = 'true';
        campo.setAttribute('maxlength', '16');
        campo.setAttribute('inputmode', 'numeric');

        campo.addEventListener('keydown', bloquearNaoDigitos);
        campo.addEventListener('input', function (e) {
            var isDelete = e && (e.inputType === 'deleteContentBackward' || e.inputType === 'deleteContentForward');
            this.value = formatarTelefone(this.value, isDelete);
        });
        campo.addEventListener('paste', function (e) {
            e.preventDefault();
            var clipboardData = e.clipboardData || window.clipboardData;
            var texto = clipboardData ? clipboardData.getData('text') : '';
            this.value = formatarTelefone(texto, false);
        });
        campo.addEventListener('blur', function () {
            var d = this.value.replace(/\D/g, '');
            this.value = d.length === 0 ? '' : formatarTelefone(d, false);
        });

        if (campo.value) {
            campo.value = formatarTelefone(campo.value, false);
        }
    }

    function aplicarMascaraCpf(campo) {
        if (!campo || campo.dataset.mascaraAplicada === 'true') return;
        campo.dataset.mascaraAplicada = 'true';
        campo.setAttribute('maxlength', '14');
        campo.setAttribute('inputmode', 'numeric');

        campo.addEventListener('keydown', bloquearNaoDigitos);
        campo.addEventListener('input', function () {
            this.value = formatarCpf(this.value);
        });
        campo.addEventListener('paste', function (e) {
            e.preventDefault();
            var clipboardData = e.clipboardData || window.clipboardData;
            var texto = clipboardData ? clipboardData.getData('text') : '';
            this.value = formatarCpf(texto);
        });
        if (campo.value) {
            campo.value = formatarCpf(campo.value);
        }
    }

    function aplicarMascaraSus(campo) {
        if (!campo || campo.dataset.mascaraAplicada === 'true') return;
        campo.dataset.mascaraAplicada = 'true';
        campo.setAttribute('maxlength', '15');
        campo.setAttribute('inputmode', 'numeric');

        campo.addEventListener('keydown', bloquearNaoDigitos);
        campo.addEventListener('input', function () {
            this.value = (this.value || '').replace(/\D/g, '').slice(0, 15);
        });
        campo.addEventListener('paste', function (e) {
            e.preventDefault();
            var clipboardData = e.clipboardData || window.clipboardData;
            var texto = clipboardData ? clipboardData.getData('text') : '';
            this.value = (texto || '').replace(/\D/g, '').slice(0, 15);
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('input[type="tel"], #telefone, [data-mascara="telefone"]').forEach(aplicarMascaraTelefone);
        document.querySelectorAll('#cpf, [data-mascara="cpf"]').forEach(aplicarMascaraCpf);
        document.querySelectorAll('#cartaoSus, [data-mascara="sus"]').forEach(aplicarMascaraSus);
    });
})();

/* ============================================================
   Busca Inteligente de Pacientes (Combobox na Recepção + Filtro na Tabela)
   ============================================================ */
(function () {
    var somenteDigitos = function (texto) { return (texto || '').replace(/\D/g, ''); };
    var semAcento = function (texto) {
        return (texto || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
    };

    document.addEventListener('DOMContentLoaded', function () {
        /* 1. Autocomplete Combobox de Paciente (/atendimentos/novo) */
        var combobox = document.querySelector('[data-paciente-combobox]');
        if (combobox) {
            var inputBusca = combobox.querySelector('[data-paciente-input]');
            var selectPaciente = document.getElementById('pacienteId');
            var selectPrioridade = document.getElementById('prioridade');
            var listaDropdown = combobox.querySelector('[data-paciente-dropdown]');
            var cardSelecionado = document.getElementById('pacienteSelecionadoCard');
            var nomeSelecionado = document.getElementById('pacienteSelecionadoNome');
            var metaSelecionado = document.getElementById('pacienteSelecionadoMeta');
            var badgeIdoso = document.getElementById('pacienteSelecionadoIdoso');
            var badgeSuperIdoso = document.getElementById('pacienteSelecionadoSuperIdoso');
            var btnTrocar = document.getElementById('btnTrocarPaciente');
            var btnLimpar = combobox.querySelector('[data-paciente-limpar]');

            var pacientes = [];
            Array.prototype.forEach.call(selectPaciente.options, function (opt) {
                if (!opt.value) return;
                pacientes.push({
                    id: opt.value,
                    nome: opt.dataset.nome || opt.text,
                    cpf: opt.dataset.cpf || '',
                    idade: opt.dataset.idade || '',
                    telefone: opt.dataset.telefone || '',
                    idoso: opt.dataset.idoso === 'true',
                    superIdoso: opt.dataset.superIdoso === 'true'
                });
            });

            var indiceAtivo = -1;
            var resultadosAtuais = [];

            function selecionarPaciente(p, focarFila) {
                if (!p) {
                    selectPaciente.value = '';
                    if (cardSelecionado) cardSelecionado.hidden = true;
                    combobox.hidden = false;
                    if (inputBusca) {
                        inputBusca.value = '';
                        inputBusca.focus();
                    }
                    if (btnLimpar) btnLimpar.hidden = true;
                    fecharDropdown();
                    return;
                }

                selectPaciente.value = p.id;
                if (inputBusca) {
                    inputBusca.value = p.nome + ' · CPF ' + p.cpf;
                }
                if (btnLimpar) btnLimpar.hidden = false;
                fecharDropdown();

                if (cardSelecionado && nomeSelecionado && metaSelecionado) {
                    nomeSelecionado.textContent = p.nome;
                    var partes = ['CPF ' + p.cpf];
                    if (p.idade) partes.push(p.idade + ' anos');
                    if (p.telefone) partes.push(p.telefone);
                    metaSelecionado.textContent = partes.join(' · ');
                    if (badgeSuperIdoso) {
                        badgeSuperIdoso.hidden = !p.superIdoso;
                    }
                    if (badgeIdoso) {
                        badgeIdoso.hidden = (!p.idoso || p.superIdoso);
                    }
                    cardSelecionado.hidden = false;
                    combobox.hidden = true;
                }

                // Hierarquia legal: 80+ Superprioridade (Lei 13.466/2017) ou 60+ Preferencial (Lei 10.048/2000)
                if (selectPrioridade) {
                    if (p.superIdoso) {
                        selectPrioridade.value = 'SUPERPREFERENCIAL';
                    } else if (p.idoso) {
                        selectPrioridade.value = 'PREFERENCIAL';
                    }
                }

                if (focarFila) {
                    var selectFila = document.getElementById('filaId');
                    if (selectFila) selectFila.focus();
                }
            }

            function fecharDropdown() {
                if (!listaDropdown) return;
                listaDropdown.hidden = true;
                if (inputBusca) inputBusca.setAttribute('aria-expanded', 'false');
                indiceAtivo = -1;
            }

            function renderizarDropdown(termoRaw) {
                if (!listaDropdown) return;
                var termo = semAcento((termoRaw || '').trim());
                var digitos = somenteDigitos(termoRaw);

                resultadosAtuais = pacientes.filter(function (p) {
                    if (!termo && !digitos) return true;
                    var nomeNorm = semAcento(p.nome);
                    var cpfDigitos = somenteDigitos(p.cpf);
                    var telDigitos = somenteDigitos(p.telefone);
                    var matchNome = termo && nomeNorm.indexOf(termo) >= 0;
                    var matchDigitos = digitos.length > 0 && (cpfDigitos.indexOf(digitos) >= 0 || telDigitos.indexOf(digitos) >= 0);
                    return matchNome || matchDigitos;
                });

                listaDropdown.innerHTML = '';
                indiceAtivo = resultadosAtuais.length > 0 ? 0 : -1;

                if (resultadosAtuais.length === 0) {
                    var vazio = document.createElement('div');
                    vazio.className = 'paciente-sugestao-vazia';
                    vazio.innerHTML = '<span>Nenhum paciente encontrado com <strong>' +
                        (termoRaw || '').replace(/[<>&"]/g, '') +
                        '</strong>.</span><a class="btn btn-secundario btn-compacto" href="/pacientes/novo">+ Cadastrar novo paciente</a>';
                    listaDropdown.appendChild(vazio);
                } else {
                    var CabecalhoInfo = document.createElement('div');
                    CabecalhoInfo.className = 'paciente-sugestao-topo';
                    CabecalhoInfo.textContent = termo
                        ? (resultadosAtuais.length + (resultadosAtuais.length === 1 ? ' paciente encontrado' : ' pacientes encontrados'))
                        : 'Pacientes cadastrados (' + resultadosAtuais.length + ') — clique ou use as setas';
                    listaDropdown.appendChild(CabecalhoInfo);

                    resultadosAtuais.slice(0, 12).forEach(function (p, idx) {
                        var item = document.createElement('button');
                        item.type = 'button';
                        item.className = 'paciente-sugestao-item' + (idx === 0 ? ' is-active' : '');
                        item.setAttribute('role', 'option');

                        var linhaPrincipal = document.createElement('div');
                        linhaPrincipal.className = 'paciente-sugestao-linha';

                        var spanNome = document.createElement('span');
                        spanNome.className = 'paciente-sugestao-nome';
                        spanNome.textContent = p.nome;
                        linhaPrincipal.appendChild(spanNome);

                        if (p.superIdoso) {
                            var tagSuper = document.createElement('span');
                            tagSuper.className = 'etiqueta-prioridade etiqueta-superpreferencial';
                            tagSuper.textContent = '80+ Superprioridade (Lei 13.466/2017)';
                            linhaPrincipal.appendChild(tagSuper);
                        } else if (p.idoso) {
                            var tagPref = document.createElement('span');
                            tagPref.className = 'etiqueta-prioridade etiqueta-preferencial';
                            tagPref.textContent = '60+ Preferencial (Lei 10.048/2000)';
                            linhaPrincipal.appendChild(tagPref);
                        }

                        var linhaMeta = document.createElement('div');
                        linhaMeta.className = 'paciente-sugestao-meta num';
                        var metas = ['CPF: ' + p.cpf];
                        if (p.idade) metas.push(p.idade + ' anos');
                        if (p.telefone) metas.push(p.telefone);
                        linhaMeta.textContent = metas.join(' · ');

                        item.appendChild(linhaPrincipal);
                        item.appendChild(linhaMeta);

                        item.addEventListener('click', function () {
                            selecionarPaciente(p, true);
                        });
                        listaDropdown.appendChild(item);
                    });
                }

                listaDropdown.hidden = false;
                if (inputBusca) inputBusca.setAttribute('aria-expanded', 'true');
            }

            function atualizarItemAtivo() {
                var itens = listaDropdown.querySelectorAll('.paciente-sugestao-item');
                itens.forEach(function (el, idx) {
                    el.classList.toggle('is-active', idx === indiceAtivo);
                    if (idx === indiceAtivo) {
                        el.scrollIntoView({ block: 'nearest' });
                    }
                });
            }

            if (inputBusca) {
                inputBusca.addEventListener('focus', function () {
                    renderizarDropdown(this.value);
                });
                inputBusca.addEventListener('input', function () {
                    selectPaciente.value = '';
                    if (btnLimpar) btnLimpar.hidden = !this.value;
                    renderizarDropdown(this.value);
                });
                inputBusca.addEventListener('keydown', function (e) {
                    var maxIdx = Math.min(resultadosAtuais.length, 12) - 1;
                    if (e.key === 'ArrowDown') {
                        e.preventDefault();
                        if (listaDropdown.hidden) {
                            renderizarDropdown(this.value);
                        } else if (maxIdx >= 0) {
                            indiceAtivo = (indiceAtivo < maxIdx) ? indiceAtivo + 1 : 0;
                            atualizarItemAtivo();
                        }
                    } else if (e.key === 'ArrowUp') {
                        e.preventDefault();
                        if (!listaDropdown.hidden && maxIdx >= 0) {
                            indiceAtivo = (indiceAtivo > 0) ? indiceAtivo - 1 : maxIdx;
                            atualizarItemAtivo();
                        }
                    } else if (e.key === 'Enter') {
                        if (!listaDropdown.hidden && indiceAtivo >= 0 && resultadosAtuais[indiceAtivo]) {
                            e.preventDefault();
                            selecionarPaciente(resultadosAtuais[indiceAtivo], true);
                        }
                    } else if (e.key === 'Escape') {
                        fecharDropdown();
                    }
                });
            }

            if (btnTrocar) {
                btnTrocar.addEventListener('click', function () {
                    selecionarPaciente(null, false);
                });
            }

            if (btnLimpar) {
                btnLimpar.addEventListener('click', function () {
                    selecionarPaciente(null, false);
                });
            }

            document.addEventListener('click', function (e) {
                if (!combobox.contains(e.target)) {
                    fecharDropdown();
                }
            });

            // Se já veio com paciente pré-selecionado na URL (?pacienteId=...)
            if (selectPaciente.value) {
                var preSelecionado = pacientes.find(function (p) { return p.id === selectPaciente.value; });
                if (preSelecionado) {
                    selecionarPaciente(preSelecionado, false);
                }
            }
        }

        /* 2. Filtro instantâneo na tabela de Pacientes (/pacientes) */
        var inputFiltroTabela = document.querySelector('[data-filtrar-tabela]');
        if (inputFiltroTabela) {
            var tabelaAlvo = document.getElementById(inputFiltroTabela.dataset.filtrarTabela);
            var contadorEl = document.getElementById('contadorPacientes');
            var avisoVazio = document.getElementById('avisoBuscaVazia');

            if (tabelaAlvo) {
                var linhas = Array.prototype.slice.call(tabelaAlvo.querySelectorAll('tbody tr'));
                inputFiltroTabela.addEventListener('input', function () {
                    var termo = semAcento(this.value.trim());
                    var digitos = somenteDigitos(this.value);
                    var visiveis = 0;

                    linhas.forEach(function (tr) {
                        var textoLinha = semAcento(tr.textContent);
                        var digitosLinha = somenteDigitos(tr.textContent);
                        var mostrar = !termo || textoLinha.indexOf(termo) >= 0 ||
                            (digitos.length > 0 && digitosLinha.indexOf(digitos) >= 0);
                        tr.hidden = !mostrar;
                        if (mostrar) visiveis++;
                    });

                    if (contadorEl) {
                        contadorEl.textContent = visiveis === 1
                            ? '1 paciente encontrado'
                            : visiveis + ' pacientes encontrados';
                    }
                    if (avisoVazio) {
                        avisoVazio.hidden = visiveis > 0;
                    }
                });
            }
        }

        /* 3. Chips rápidos de encaminhamento e persistência de sala (/atendimentos/guiche) */
        document.querySelectorAll('[data-chip-encaminhamento]').forEach(function (chip) {
            chip.addEventListener('click', function () {
                var targetId = this.getAttribute('data-target');
                var input = document.getElementById(targetId);
                if (!input) return;
                var valorChip = this.getAttribute('data-chip-encaminhamento');
                var atual = input.value.trim();

                if (!atual) {
                    input.value = valorChip;
                } else if (atual.toLowerCase().indexOf(valorChip.toLowerCase()) < 0) {
                    input.value = atual + ' · ' + valorChip;
                }
                this.classList.add('is-selected');
                input.focus();
            });
        });

        // Sincronizar o campo "Sala ou consultório" principal com os formulários de chamada individual da tabela
        var inputGuichePrincipal = document.getElementById('guiche');
        if (inputGuichePrincipal) {
            try {
                if (!inputGuichePrincipal.value) {
                    var salvo = localStorage.getItem('fs-guiche-atendimento');
                    if (salvo) inputGuichePrincipal.value = salvo;
                }
            } catch (e) {}

            var sincronizarGuiches = function () {
                var val = inputGuichePrincipal.value;
                try {
                    if (val && val.trim()) {
                        localStorage.setItem('fs-guiche-atendimento', val.trim());
                    }
                } catch (e) {}
                document.querySelectorAll('.input-guiche-sincronizado').forEach(function (hiddenInput) {
                    hiddenInput.value = val;
                });
            };

            inputGuichePrincipal.addEventListener('input', sincronizarGuiches);
            sincronizarGuiches();
        }
    });
})();

/* ============================================================
   Confirmação de Ações Críticas (Modal <dialog>)
   ============================================================ */
(function () {
    document.addEventListener('DOMContentLoaded', function () {
        var dialogo = document.getElementById('confirmacao');
        if (!dialogo || typeof dialogo.showModal !== 'function') return;

        var titulo = dialogo.querySelector('[data-confirmacao-titulo]');
        var texto = dialogo.querySelector('[data-confirmacao-texto]');
        var botaoConfirmar = dialogo.querySelector('[data-confirmacao-sim]');
        var formularioPendente = null;

        document.addEventListener('submit', function (evento) {
            var formulario = evento.target;
            if (!formulario.dataset.confirmar) return;

            evento.preventDefault();
            formularioPendente = formulario;
            if (titulo) titulo.textContent = formulario.dataset.confirmar;
            if (texto) {
                texto.textContent = formulario.dataset.consequencia || '';
                texto.hidden = !formulario.dataset.consequencia;
            }
            if (botaoConfirmar) botaoConfirmar.textContent = formulario.dataset.acao || 'Confirmar';
            dialogo.showModal();
        });

        dialogo.addEventListener('close', function () {
            if (dialogo.returnValue === 'sim' && formularioPendente) {
                formularioPendente.submit();
            }
            formularioPendente = null;
            dialogo.returnValue = '';
        });
    });
})();

/* ============================================================
   Mecanismo de Abertura / Fechamento do Painel Lateral (Sidebar)
   ============================================================ */
(function () {
    document.addEventListener('DOMContentLoaded', function () {
        var btn = document.getElementById('btnToggleSidebar');
        var sidebar = document.getElementById('appSidebar') || document.querySelector('.app-sidebar');
        if (!btn || !sidebar) return;

        var estaRecolhida = document.documentElement.classList.contains('sidebar-recolhida-ativa') ||
                            sidebar.classList.contains('is-collapsed');
        var timerAnimacao;

        function aplicarEstado(recolhida, animar) {
            if (timerAnimacao) window.clearTimeout(timerAnimacao);
            sidebar.classList.toggle('is-transitioning', Boolean(animar));
            sidebar.classList.toggle('is-expanding', Boolean(animar) && !recolhida);
            if (recolhida) {
                sidebar.classList.add('is-collapsed');
                document.documentElement.classList.add('sidebar-recolhida-ativa');
                btn.setAttribute('aria-expanded', 'false');
                btn.setAttribute('title', 'Expandir menu lateral (Alt + M)');
                try { localStorage.setItem('fs-sidebar-recolhida', 'true'); } catch (e) {}
            } else {
                sidebar.classList.remove('is-collapsed');
                document.documentElement.classList.remove('sidebar-recolhida-ativa');
                btn.setAttribute('aria-expanded', 'true');
                btn.setAttribute('title', 'Recolher menu lateral (Alt + M)');
                try { localStorage.setItem('fs-sidebar-recolhida', 'false'); } catch (e) {}
            }
            if (animar) {
                timerAnimacao = window.setTimeout(function () {
                    sidebar.classList.remove('is-transitioning');
                    sidebar.classList.remove('is-expanding');
                }, 240);
            } else {
                sidebar.classList.remove('is-expanding');
            }
        }

        aplicarEstado(estaRecolhida, false);

        btn.addEventListener('click', function (e) {
            e.preventDefault();
            var recolher = !sidebar.classList.contains('is-collapsed') &&
                           !document.documentElement.classList.contains('sidebar-recolhida-ativa');
            aplicarEstado(recolher, true);
        });

        // Atalho de teclado rápido: Alt + M
        window.addEventListener('keydown', function (e) {
            if (e.altKey && (e.key === 'm' || e.key === 'M')) {
                e.preventDefault();
                btn.click();
            }
        });
    });
})();
