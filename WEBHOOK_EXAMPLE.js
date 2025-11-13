/**
 * EXEMPLO DE WEBHOOK HANDLER (Node.js/Express)
 *
 * Este arquivo mostra como implementar um endpoint para receber
 * notificações do PagBank no seu backend
 */

const express = require('express');
const crypto = require('crypto');
const router = express.Router();

// Token do PagBank (mesmo usado nas requisições)
const PAGBANK_TOKEN = process.env.PAGBANK_TOKEN;

/**
 * Endpoint para receber webhooks do PagBank
 * POST /webhook/pagbank
 */
router.post('/webhook/pagbank', async (req, res) => {
    try {
        console.log('📩 Webhook recebido:', JSON.stringify(req.body, null, 2));

        // Validar autenticidade (opcional mas recomendado)
        const signature = req.headers['x-pagbank-signature'];
        if (!validarAssinatura(req.body, signature)) {
            console.error('❌ Assinatura inválida');
            return res.status(401).json({ error: 'Invalid signature' });
        }

        const notification = req.body;

        // Processar baseado no tipo de evento
        switch (notification.event) {
            case 'charge.paid':
                await processarPagamentoPago(notification);
                break;

            case 'charge.declined':
                await processarPagamentoRecusado(notification);
                break;

            case 'charge.canceled':
                await processarPagamentoCancelado(notification);
                break;

            case 'transfer.completed':
                await processarTransferenciaCompleta(notification);
                break;

            case 'transfer.failed':
                await processarTransferenciaFalhou(notification);
                break;

            default:
                console.log('⚠️ Evento não tratado:', notification.event);
        }

        // Sempre retornar 200 OK para o PagBank
        res.status(200).json({ received: true });

    } catch (error) {
        console.error('❌ Erro ao processar webhook:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

/**
 * Processa pagamento PIX confirmado
 */
async function processarPagamentoPago(notification) {
    console.log('✅ Pagamento confirmado!');

    const { id, reference_id, data } = notification;
    const valor = data.amount?.value || 0;
    const valorEmReais = valor / 100;

    // 1. Atualizar saldo do usuário no banco de dados
    await atualizarSaldoUsuario(reference_id, valorEmReais);

    // 2. Criar registro de transação
    await criarTransacao({
        tipo: 'DEPOSITO',
        valor: valorEmReais,
        status: 'CONCLUIDA',
        referenceId: reference_id,
        pagbankId: id,
        metodoPagamento: 'PIX'
    });

    // 3. Enviar notificação push para o usuário
    await enviarNotificacaoPush(reference_id, {
        titulo: 'Depósito Confirmado! 💰',
        mensagem: `Seu depósito de R$ ${valorEmReais.toFixed(2)} foi confirmado!`,
        tipo: 'PAGAMENTO_CONFIRMADO'
    });

    console.log(`💰 Saldo atualizado: +R$ ${valorEmReais.toFixed(2)}`);
}

/**
 * Processa pagamento recusado
 */
async function processarPagamentoRecusado(notification) {
    console.log('❌ Pagamento recusado');

    const { reference_id } = notification;

    // Atualizar status da transação
    await atualizarTransacao(reference_id, {
        status: 'FALHOU',
        motivoFalha: 'Pagamento recusado pelo banco'
    });

    // Notificar usuário
    await enviarNotificacaoPush(reference_id, {
        titulo: 'Pagamento Recusado',
        mensagem: 'Seu pagamento não foi processado. Tente novamente.',
        tipo: 'PAGAMENTO_RECUSADO'
    });
}

/**
 * Processa transferência completa (saque)
 */
async function processarTransferenciaCompleta(notification) {
    console.log('✅ Transferência completa');

    const { reference_id, data } = notification;
    const valor = data.amount?.value || 0;
    const valorEmReais = valor / 100;

    // 1. Atualizar status do saque
    await atualizarTransacao(reference_id, {
        status: 'CONCLUIDA',
        dataProcessamento: new Date()
    });

    // 2. Deduzir do saldo (se ainda não foi deduzido)
    await deduzirSaldoUsuario(reference_id, valorEmReais);

    // 3. Notificar usuário
    await enviarNotificacaoPush(reference_id, {
        titulo: 'Saque Concluído! ✅',
        mensagem: `Seu saque de R$ ${valorEmReais.toFixed(2)} foi processado!`,
        tipo: 'SAQUE_CONCLUIDO'
    });
}

/**
 * Processa transferência falhou (saque)
 */
async function processarTransferenciaFalhou(notification) {
    console.log('❌ Transferência falhou');

    const { reference_id, data } = notification;
    const valor = data.amount?.value || 0;
    const valorEmReais = valor / 100;
    const motivo = data.error?.message || 'Erro desconhecido';

    // 1. Atualizar status do saque
    await atualizarTransacao(reference_id, {
        status: 'FALHOU',
        motivoFalha: motivo
    });

    // 2. Estornar saldo ao usuário
    await estornarSaldoUsuario(reference_id, valorEmReais);

    // 3. Notificar usuário
    await enviarNotificacaoPush(reference_id, {
        titulo: 'Saque Falhou',
        mensagem: `Seu saque não foi processado. Valor estornado.`,
        tipo: 'SAQUE_FALHOU'
    });
}

/**
 * Valida assinatura do webhook (segurança)
 */
function validarAssinatura(payload, signature) {
    if (!signature) return false;

    const hmac = crypto.createHmac('sha256', PAGBANK_TOKEN);
    hmac.update(JSON.stringify(payload));
    const expectedSignature = hmac.digest('hex');

    return signature === expectedSignature;
}

// ============ FUNÇÕES AUXILIARES (implementar com seu banco de dados) ============

async function atualizarSaldoUsuario(referenceId, valor) {
    // Implementar: adicionar valor ao saldo do usuário
    console.log(`💾 Atualizando saldo: +R$ ${valor}`);
}

async function deduzirSaldoUsuario(referenceId, valor) {
    // Implementar: deduzir valor do saldo do usuário
    console.log(`💾 Deduzindo saldo: -R$ ${valor}`);
}

async function estornarSaldoUsuario(referenceId, valor) {
    // Implementar: estornar valor ao saldo do usuário
    console.log(`💾 Estornando saldo: +R$ ${valor}`);
}

async function criarTransacao(dados) {
    // Implementar: criar registro de transação no banco
    console.log(`💾 Criando transação:`, dados);
}

async function atualizarTransacao(referenceId, dados) {
    // Implementar: atualizar transação existente
    console.log(`💾 Atualizando transação ${referenceId}:`, dados);
}

async function enviarNotificacaoPush(referenceId, notificacao) {
    // Implementar: enviar notificação push (FCM, OneSignal, etc)
    console.log(`📱 Enviando push:`, notificacao);
}

module.exports = router;

/**
 * EXEMPLO DE USO NO EXPRESS:
 *
 * const webhookRouter = require('./routes/webhook');
 * app.use('/webhook', webhookRouter);
 *
 * CONFIGURAR NO PAGBANK:
 * https://seu-servidor.com/webhook/pagbank
 *
 * TESTAR LOCALMENTE COM NGROK:
 * 1. Instalar: npm install -g ngrok
 * 2. Rodar: ngrok http 3000
 * 3. URL gerada: https://abc123.ngrok.io
 * 4. Configurar: https://abc123.ngrok.io/webhook/pagbank
 */

