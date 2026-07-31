#!/usr/bin/env python3
"""Resume a saida de `jfr print` numa linha por metrica.

`jfr print` cospe um evento por segundo de gravacao (milhares de blocos
repetidos). Para comparar duas medicoes so interessam os ultimos valores, ja
sob carga: heap apos GC, heap committed, metaspace usado e o pico de classes
carregadas. Le da entrada padrao, escreve o resumo na saida padrao.

Usado por utilitarios/medir_memoria_headless.sh.
"""
import re
import sys


def resumir(texto):
    blocos = re.findall(r"(jdk\.\w+) \{(.*?)\n\}", texto, re.S)

    heap_apos_gc = None
    heap_committed = None
    metaspace_usado = None
    classes = 0

    for nome, corpo in blocos:
        apos_gc = 'when = "After GC"' in corpo
        if nome == "jdk.GCHeapSummary" and apos_gc:
            achado = re.search(r"heapUsed = (.+)", corpo)
            if achado:
                heap_apos_gc = achado.group(1).strip()
            achado = re.search(r"committedSize = (.+)", corpo)
            if achado:
                heap_committed = achado.group(1).strip()
        elif nome == "jdk.MetaspaceSummary" and apos_gc:
            achado = re.search(
                r"metaspace = \{\s*committed = .+?\n\s*used = ([^\n]+)", corpo, re.S)
            if achado:
                metaspace_usado = achado.group(1).strip()
        elif nome == "jdk.ClassLoadingStatistics":
            achado = re.search(r"loadedClassCount = (\d+)", corpo)
            if achado:
                classes = max(classes, int(achado.group(1)))

    return "\n".join([
        "heap usado apos GC:  %s" % (heap_apos_gc or "n/a"),
        "heap committed:      %s" % (heap_committed or "n/a"),
        "metaspace usado:     %s" % (metaspace_usado or "n/a"),
        "classes carregadas:  %s" % (classes or "n/a"),
    ])


if __name__ == "__main__":
    print(resumir(sys.stdin.read()))
