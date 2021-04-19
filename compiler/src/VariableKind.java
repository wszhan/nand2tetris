public enum VariableKind {
    STATIC,
    FIELD,
    ARG,
    VAR, // subroutine local variable
    NONE; // unknown; for error-free jack code, this must be subroutine or class name
}
