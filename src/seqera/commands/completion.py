"""
Shell completion generation for Seqera CLI.

Generate shell completion scripts for bash, zsh, and fish.
"""

import sys
from enum import Enum
from typing import Annotated

import typer


class Shell(str, Enum):
    """Supported shells for completion."""

    BASH = "bash"
    ZSH = "zsh"
    FISH = "fish"


# Completion scripts
BASH_COMPLETION = """
_seqera_completion() {
    local IFS=$'\\n'
    COMPREPLY=( $(env COMP_WORDS="${COMP_WORDS[*]}" \\
                   COMP_CWORD=$COMP_CWORD \\
                   _SEQERA_COMPLETE=complete_bash $1) )
    return 0
}

complete -o default -F _seqera_completion seqera
"""

ZSH_COMPLETION = """
#compdef seqera

_seqera_completion() {
    local -a completions
    local -a completions_with_descriptions
    local -a response
    (( ! $+commands[seqera] )) && return 1

    response=("${(@f)$(env COMP_WORDS="${words[*]}" COMP_CWORD=$((CURRENT-1)) _SEQERA_COMPLETE=complete_zsh seqera)}")

    for key descr in ${(kv)response}; do
        if [[ "$descr" == "_" ]]; then
            completions+=("$key")
        else
            completions_with_descriptions+=("$key":"$descr")
        fi
    done

    if [ -n "$completions_with_descriptions" ]; then
        _describe -V unsorted completions_with_descriptions -U
    fi

    if [ -n "$completions" ]; then
        compadd -U -V unsorted -a completions
    fi
}

compdef _seqera_completion seqera
"""

FISH_COMPLETION = """
function _seqera_completion
    set -l response (env _SEQERA_COMPLETE=complete_fish COMP_WORDS=(commandline -cp) COMP_CWORD=(commandline -t) seqera)

    for completion in $response
        set -l metadata (string split "," -- $completion)

        if [ $metadata[1] != "_" ]
            echo -e $metadata[1]\\t$metadata[2]
        else
            echo -e $metadata[2]
        end
    end
end

complete --no-files --command seqera --arguments '(_seqera_completion)'
"""


def generate_completion(
    shell: Annotated[
        Shell,
        typer.Argument(help="Shell type to generate completion for"),
    ],
) -> None:
    """Generate shell completion script.

    To enable completion, add the output to your shell configuration file:

    Bash:   eval "$(seqera generate-completion bash)"
            or source <(seqera generate-completion bash)

    Zsh:    eval "$(seqera generate-completion zsh)"
            or source <(seqera generate-completion zsh)

    Fish:   seqera generate-completion fish > ~/.config/fish/completions/seqera.fish
    """
    if shell == Shell.BASH:
        typer.echo(BASH_COMPLETION.strip())
    elif shell == Shell.ZSH:
        typer.echo(ZSH_COMPLETION.strip())
    elif shell == Shell.FISH:
        typer.echo(FISH_COMPLETION.strip())
    else:
        typer.echo(f"Error: Unsupported shell: {shell}", err=True)
        sys.exit(1)
