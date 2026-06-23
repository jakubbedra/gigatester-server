package com.konfyrm.gigatester.crosswords.service;

import com.konfyrm.gigatester.crosswords.domain.entity.Crossword;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordPlayer;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordState;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordStateTerm;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordTerm;
import com.konfyrm.gigatester.crosswords.domain.entity.enums.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class CrosswordGeneratorService {

    private static final int MAX_GRID_SIZE = 25;

    record Placement(CrosswordTerm term, int row, int col, Direction direction, int intersections) {}

    public CrosswordState generate(Crossword crossword, int numberOfWords) {
        List<CrosswordTerm> terms = crossword.getTerms();
        if (terms == null || terms.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Crossword has no terms");
        }

        List<CrosswordTerm> selected = new ArrayList<>(terms);
        Collections.shuffle(selected);
        selected = new ArrayList<>(selected.subList(0, Math.min(numberOfWords, selected.size())));
        selected.sort(Comparator.comparingInt(t -> -t.getTerm().length()));

        int gridSize = computeGridSize(selected);
        char[][] grid = new char[gridSize][gridSize];
        for (char[] row : grid) Arrays.fill(row, CrosswordState.INACTIVE_FIELD);

        List<Placement> placed = new ArrayList<>();
        if (!backtrack(selected, 0, grid, gridSize, placed)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not generate a crossword layout for the given terms");
        }

        return buildState(crossword, grid, gridSize, placed);
    }

    private int computeGridSize(List<CrosswordTerm> terms) {
        int maxLen = terms.stream().mapToInt(t -> t.getTerm().length()).max().orElse(5);
        int totalLen = terms.stream().mapToInt(t -> t.getTerm().length()).sum();
        int estimated = (int) Math.sqrt(totalLen * 2.5);
        return Math.min(Math.max(maxLen + 4, estimated), MAX_GRID_SIZE);
    }

    private boolean backtrack(List<CrosswordTerm> terms, int index, char[][] grid, int size, List<Placement> placed) {
        if (index == terms.size()) return true;

        CrosswordTerm term = terms.get(index);
        String word = term.getTerm().toUpperCase();

        List<Placement> candidates;

        if (index == 0) {
            int row = size / 2;
            int col = (size - word.length()) / 2;
            candidates = List.of(new Placement(term, row, col, Direction.ACROSS, 0));
        } else {
            candidates = new ArrayList<>();
            for (Direction dir : Direction.values()) {
                for (int r = 0; r < size; r++) {
                    for (int c = 0; c < size; c++) {
                        if (isValidPlacement(grid, word, r, c, dir, size)) {
                            int intersections = countIntersections(grid, word, r, c, dir, size);
                            candidates.add(new Placement(term, r, c, dir, intersections));
                        }
                    }
                }
            }
            candidates = candidates.stream()
                    .filter(p -> p.intersections() > 0)
                    .sorted(Comparator.comparingInt(p -> -p.intersections()))
                    .toList();
        }

        for (Placement p : candidates) {
            char[][] backup = copyGrid(grid, size);
            placeWord(grid, word, p.row(), p.col(), p.direction());
            placed.add(p);

            if (backtrack(terms, index + 1, grid, size, placed)) return true;

            restoreGrid(grid, backup, size);
            placed.remove(placed.size() - 1);
        }

        return false;
    }

    private boolean isValidPlacement(char[][] grid, String word, int row, int col, Direction dir, int size) {
        int len = word.length();
        int dr = dir == Direction.DOWN ? 1 : 0;
        int dc = dir == Direction.ACROSS ? 1 : 0;

        int endRow = row + dr * (len - 1);
        int endCol = col + dc * (len - 1);

        if (row < 0 || col < 0 || endRow >= size || endCol >= size) return false;

        // Cell immediately before the word must be empty
        int preRow = row - dr, preCol = col - dc;
        if (preRow >= 0 && preCol >= 0 && grid[preRow][preCol] != CrosswordState.INACTIVE_FIELD) return false;

        // Cell immediately after the word must be empty
        int postRow = row + dr * len, postCol = col + dc * len;
        if (postRow < size && postCol < size && grid[postRow][postCol] != CrosswordState.INACTIVE_FIELD) return false;

        for (int i = 0; i < len; i++) {
            int r = row + dr * i;
            int c = col + dc * i;
            char existing = grid[r][c];
            char letter = word.charAt(i);

            if (existing != CrosswordState.INACTIVE_FIELD) {
                if (existing != letter) return false;
                // Valid intersection — no adjacency check needed for this cell
            } else {
                // Empty cell — adjacent perpendicular cells must be empty to avoid merging words
                if (dir == Direction.ACROSS) {
                    if (r > 0 && grid[r - 1][c] != CrosswordState.INACTIVE_FIELD) return false;
                    if (r < size - 1 && grid[r + 1][c] != CrosswordState.INACTIVE_FIELD) return false;
                } else {
                    if (c > 0 && grid[r][c - 1] != CrosswordState.INACTIVE_FIELD) return false;
                    if (c < size - 1 && grid[r][c + 1] != CrosswordState.INACTIVE_FIELD) return false;
                }
            }
        }

        return true;
    }

    private int countIntersections(char[][] grid, String word, int row, int col, Direction dir, int size) {
        int len = word.length();
        int dr = dir == Direction.DOWN ? 1 : 0;
        int dc = dir == Direction.ACROSS ? 1 : 0;
        int count = 0;
        for (int i = 0; i < len; i++) {
            int r = row + dr * i;
            int c = col + dc * i;
            if (r >= 0 && r < size && c >= 0 && c < size && grid[r][c] != CrosswordState.INACTIVE_FIELD) {
                count++;
            }
        }
        return count;
    }

    private void placeWord(char[][] grid, String word, int row, int col, Direction dir) {
        int dr = dir == Direction.DOWN ? 1 : 0;
        int dc = dir == Direction.ACROSS ? 1 : 0;
        for (int i = 0; i < word.length(); i++) {
            grid[row + dr * i][col + dc * i] = word.charAt(i);
        }
    }

    private char[][] copyGrid(char[][] grid, int size) {
        char[][] copy = new char[size][size];
        for (int i = 0; i < size; i++) copy[i] = Arrays.copyOf(grid[i], size);
        return copy;
    }

    private void restoreGrid(char[][] grid, char[][] backup, int size) {
        for (int i = 0; i < size; i++) System.arraycopy(backup[i], 0, grid[i], 0, size);
    }

    private String drawHand(List<Character> pool, int offset) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < Math.min(offset + 5, pool.size()); i++) {
            sb.append(pool.get(i));
        }
        return sb.toString();
    }

    private CrosswordState buildState(Crossword crossword, char[][] grid, int size, List<Placement> placed) {
        // Find bounding box and crop grid
        int minRow = size, maxRow = 0, minCol = size, maxCol = 0;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (grid[r][c] != CrosswordState.INACTIVE_FIELD) {
                    minRow = Math.min(minRow, r);
                    maxRow = Math.max(maxRow, r);
                    minCol = Math.min(minCol, c);
                    maxCol = Math.max(maxCol, c);
                }
            }
        }

        int width = maxCol - minCol + 1;
        int height = maxRow - minRow + 1;

        StringBuilder solution = new StringBuilder();
        StringBuilder current = new StringBuilder();

        for (int r = minRow; r <= maxRow; r++) {
            for (int c = minCol; c <= maxCol; c++) {
                char ch = grid[r][c];
                solution.append(ch);
                current.append(ch == CrosswordState.INACTIVE_FIELD ? CrosswordState.INACTIVE_FIELD : CrosswordState.UNCOVERED_FIELD);
            }
        }

        final int offsetRow = minRow;
        final int offsetCol = minCol;

        List<CrosswordStateTerm> stateTerms = placed.stream()
                .map(p -> CrosswordStateTerm.builder()
                        .crosswordTerm(p.term())
                        .row(p.row() - offsetRow)
                        .column(p.col() - offsetCol)
                        .direction(p.direction())
                        .build())
                .toList();

        List<Character> pool = new ArrayList<>();
        for (char ch : solution.toString().toCharArray()) {
            if (ch != CrosswordState.INACTIVE_FIELD) pool.add(ch);
        }
        Collections.shuffle(pool);

        CrosswordPlayer human = CrosswordPlayer.builder()
                .handLetters(drawHand(pool, 0))
                .points(0)
                .current(true)
                .bot(false)
                .build();

        CrosswordPlayer bot = CrosswordPlayer.builder()
                .handLetters(drawHand(pool, 5))
                .points(0)
                .current(false)
                .bot(true)
                .build();

        return CrosswordState.builder()
                .solutionGrid(solution.toString())
                .currentGrid(current.toString())
                .width(width)
                .height(height)
                .crossword(crossword)
                .players(new ArrayList<>(List.of(human, bot)))
                .terms(stateTerms)
                .build();
    }

}
