# Docs: https://devenv.sh/basics/
{ pkgs, inputs, ... }:
let
  # https://devenv.sh/common-patterns/#getting-a-recent-version-of-a-package-from-nixpkgs-unstable
  pkgs-latest = inputs.nixpkgs-unstable.legacyPackages.${pkgs.system};
in
{

  languages = {
    # Docs: https://devenv.sh/languages/
    nix.enable = true;
    kotlin.enable = true; # pulls in JDK; override JDK with: java.jdk.package = pkgs.temurin-bin-17;
  };

  # Android SDK — Docs: https://devenv.sh/integrations/android/
  android = {
    enable = true;
    platforms.version = [ "36" ];
    buildTools.version = [ "36.0.0" ];
    cmdLineTools.version = "11.0";
    # Uncomment for native code (NDK):
    # ndk.version = [ "26.3.11579264" ];
    # Disabled to reduce disk usage (~46GB → ~3-5GB)
    emulator.enable = false;
    systemImages.enable = false;
    sources.enable = false;
    abis = [ "arm64-v8a" ]; # add "x86_64" if you re-enable the emulator
  };

  packages = with pkgs; [
    gradle
    go-task

    # Search for packages: https://search.nixos.org/packages?channel=unstable&query=cowsay
    # (note: this searches on unstable channel, you might need to use pkgs-latest for some):
    #pkgs-latest.task-keeper
  ];

  scripts = { }; # Docs: https://devenv.sh/scripts/

  git-hooks.hooks = {
    # Docs: https://devenv.sh/pre-commit-hooks/
    # list of pre-configured hooks: https://devenv.sh/reference/options/#pre-commithooks
    nil.enable = true; # nix lsp
    nixpkgs-fmt.enable = true; # nix formatting
  };

  difftastic.enable = true; # enable semantic diffs - https://devenv.sh/integrations/difftastic/
}
