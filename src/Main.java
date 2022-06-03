/*
 * Vincent Gagnon - GAGV25059800
 */

public class Main {

	public static void main(String[] args) {
		Sudoku sudoku = new Sudoku(args[0]);

		String solution = sudoku.solveSudoku();

		System.out.println("Solution: " + solution);
		
		System.out.println("Grid form:");

		for (int x = 0; x < solution.length(); x++) {
			System.out.print(" " + solution.charAt(x) + " ");
			if (x % 9 == 8) {
				System.out.println();
			}
		}
	}

}
