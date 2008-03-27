

/**
 * Implementations of this interface are used to take a NoteList
 * and transform it into a new NoteList, using some kind of musical operation.
 * These will be combined in various ways to generate the fractal music.
 * @author Myron
 */
public interface NoteListTransformer {
    NoteList transform(NoteList input);
}
