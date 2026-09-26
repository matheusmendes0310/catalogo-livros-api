$ErrorActionPreference = 'Stop'

try {
    $pastaProjeto = Split-Path -Parent $PSScriptRoot
    Set-Location -LiteralPath $pastaProjeto

    try {
        $livros = Invoke-RestMethod -Uri 'http://localhost:8080/livros' -TimeoutSec 10
    } catch {
        throw 'Inicie a API com iniciar.cmd, aguarde a inicializacao e execute este arquivo novamente.'
    }

    if ($livros.Count -gt 0) {
        throw 'A lista precisa estar vazia. Reinicie a API antes de executar a colecao completa.'
    }

    $pastaFerramenta = Join-Path $pastaProjeto '.tools\postman-cli'
    $executavel = Join-Path $pastaFerramenta 'postman-cli.exe'

    if (-not (Test-Path -LiteralPath $executavel)) {
        Write-Host 'Baixando o executor oficial do Postman...'
        New-Item -ItemType Directory -Path $pastaFerramenta -Force | Out-Null
        $arquivoZip = Join-Path $pastaProjeto '.tools\postman-cli.zip'
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -UseBasicParsing -Uri 'https://dl-cli.pstmn.io/download/latest/win64' -OutFile $arquivoZip
        Expand-Archive -LiteralPath $arquivoZip -DestinationPath $pastaFerramenta -Force
    }

    $assinatura = Get-AuthenticodeSignature -FilePath $executavel
    if ($assinatura.Status -ne 'Valid') {
        throw 'A assinatura do executor do Postman nao foi validada. Baixe novamente a ferramenta oficial.'
    }

    $pastaRelatorios = Join-Path $PSScriptRoot 'relatorios'
    New-Item -ItemType Directory -Path $pastaRelatorios -Force | Out-Null
    & $executavel collection run (Join-Path $PSScriptRoot 'Catalogo-Livros.postman_collection.json') `
        --reporters cli,json,html `
        --reporter-json-export (Join-Path $pastaRelatorios 'resultado.json') `
        --reporter-html-export (Join-Path $pastaRelatorios 'resultado.html')

    exit $LASTEXITCODE
} catch {
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}
