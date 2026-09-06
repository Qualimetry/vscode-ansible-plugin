import * as vscode from 'vscode';
import * as path from 'path';
import * as fs from 'fs';
import { execSync } from 'child_process';

export interface AnsibleAnalyzerConfig {
    enabled: boolean;
    javaHome: string;
}

export function getConfiguration(): AnsibleAnalyzerConfig {
    const config = vscode.workspace.getConfiguration('ansibleAnalyzer');
    return {
        enabled: config.get<boolean>('enabled', true),
        javaHome: config.get<string>('java.home', ''),
    };
}

export function findJavaExecutable(configuredPath?: string): string | undefined {
    const ext = process.platform === 'win32' ? '.exe' : '';
    if (configuredPath) {
        const candidate = path.join(configuredPath, 'bin', `java${ext}`);
        if (fs.existsSync(candidate)) return candidate;
        if (fs.existsSync(configuredPath) && (configuredPath.endsWith('java') || configuredPath.endsWith('java.exe'))) return configuredPath;
    }
    const javaHome = process.env['JAVA_HOME'];
    if (javaHome) {
        const candidate = path.join(javaHome, 'bin', `java${ext}`);
        if (fs.existsSync(candidate)) return candidate;
    }
    try {
        const cmd = process.platform === 'win32' ? 'where java' : 'which java';
        const result = execSync(cmd, { encoding: 'utf8', timeout: 5000 }).trim();
        const firstLine = result.split(/\r?\n/)[0];
        if (firstLine && fs.existsSync(firstLine)) return firstLine;
    } catch {
        // ignore
    }
    return undefined;
}

export function getJavaVersion(javaExe: string): number | undefined {
    try {
        const cmd = `"${javaExe}" -version 2>&1`;
        const output = execSync(cmd, {
            encoding: 'utf8',
            timeout: 10000,
        });
        return parseJavaVersion(output || '');
    } catch (err: unknown) {
        if (err && typeof err === 'object' && 'stderr' in err) {
            const stderr = (err as { stderr: string }).stderr;
            if (stderr) return parseJavaVersion(stderr);
        }
        return undefined;
    }
}

function parseJavaVersion(output: string): number | undefined {
    const match = output.match(/version\s+"(\d+)(?:\.(\d+))?/);
    if (!match) return undefined;
    const major = parseInt(match[1], 10);
    if (major === 1 && match[2]) return parseInt(match[2], 10);
    return major;
}
