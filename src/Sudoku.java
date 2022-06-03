import java.util.ArrayList;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import stev.booleans.And;
import stev.booleans.BooleanFormula;
import stev.booleans.Implies;
import stev.booleans.Not;
import stev.booleans.Or;
import stev.booleans.PropositionalVariable;

public class Sudoku {
	// -1 means there's nothing in the case, also cases are shifted -1, roes,
	// columns and values go from 0 to 8
	private int cases[][] = new int[9][9];
	private ArrayList<BooleanFormula> formulas = new ArrayList<BooleanFormula>();

	// represents the presence of the number k in the row i and column j
	PropositionalVariable[][][] Xijk = new PropositionalVariable[9][9][9];

	public Sudoku(String puzzleString) {
		fillCasesFromPuzzleString(puzzleString);

		initializeSudokuRules();

		fillInKnownCases();

	}

	public String solveSudoku() {
		And bigFormula = new And();
		for (BooleanFormula formula : formulas) {
			bigFormula.addOperand(formula);
		}

		BooleanFormula cnf = BooleanFormula.toCnf(bigFormula);

		int[][] clauses = cnf.getClauses();

		ISolver solver = SolverFactory.newDefault();
		solver.setExpectedNumberOfClauses(clauses.length);
		solver.newVar(729);

		for (int x = 0; x < clauses.length; x++) {
			try {
				solver.addClause(new VecInt(clauses[x]));
			} catch (ContradictionException e) {
				e.printStackTrace();
			}
		}

		IProblem problem = solver;

		try {
			if (problem.isSatisfiable()) {
				return convertModelToPuzzleString(problem.findModel());
			} else {
				System.out.println("Problem is not satisfiable...");
				return "Problem is no satisfiable...";
			}
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		return "";
	}

	private String convertModelToPuzzleString(int[] model) {
		String puzzleString = "";
		int[] tempValues = new int[9];

		for (int x = 0; x < model.length; x++) {
			tempValues[x % 9] = model[x];

			if (x % 9 == 8) {
				int positiveValuesCounter = 0;
				for (int y = 0; y < 9; y++) {

					if (tempValues[y] > -1) {
						positiveValuesCounter++;
						puzzleString += y + 1;
					}
					if (positiveValuesCounter > 1) {
						System.out.println("Error in solution, multiple values in one case");
						return "ERROR";
					}
				}

				tempValues = new int[9];
			}
		}

		return puzzleString;
	}

	private void fillInKnownCases() {
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				int k = cases[i][j];
				if (k != -1) {

					formulas.add(Xijk[i][j][k]);

				}
			}
		}
	}

	private void initializeSudokuRules() {
		initializePropositionalVariables();

		createOneNumberPerCaseFormulas();

		createOneNumberPerRowFormulas();

		createOneNumberPerColumnFormulas();

		createOneNumberPer3By3GridFormulas();
	}

	private void createOneNumberPer3By3GridFormulas() {
		for (int k = 0; k < 9; k++) {
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					Or or = new Or();
					for (int i2 = 0; i2 < 3; i2++) {
						for (int j2 = 0; j2 < 3; j2++) {
							or.addOperand(Xijk[3 * i + i2][3 * j + j2][k]);

							Implies implies = new Implies(null, null);
							And and = new And();
							for (int x = 3 * i; x < 3 * i + 3; x++) {
								for (int y = 3 * j; y < 3 * j + 3; y++) {
									if (x == 3 * i + i2 && y == 3 * j + j2) {
										continue;
									}
									and.addOperand(new Not(Xijk[x][y][k]));
								}
							}

							implies.setLeft(Xijk[3 * i + i2][3 * j + j2][k]);
							implies.setRight(and);
							formulas.add(implies);
						}
					}
					formulas.add(or);
				}
			}
		}
	}

	private void createOneNumberPerColumnFormulas() {
		for (int k = 0; k < 9; k++) {
			for (int j = 0; j < 9; j++) {
				Or or = new Or();

				for (int i = 0; i < 9; i++) {
					or.addOperand(Xijk[i][j][k]);

					Implies implies = new Implies(null, null);
					And and = new And();

					for (int x = 0; x < 9; x++) {
						if (x != i) {
							and.addOperand(new Not(Xijk[x][j][k]));
						}
					}
					implies.setLeft(Xijk[i][j][k]);
					implies.setRight(and);
					formulas.add(implies);
				}

				formulas.add(or);
			}
		}
	}

	private void createOneNumberPerRowFormulas() {
		for (int k = 0; k < 9; k++) {
			for (int i = 0; i < 9; i++) {
				Or or = new Or();

				for (int j = 0; j < 9; j++) {
					Implies implies = new Implies(null, null);

					or.addOperand(Xijk[i][j][k]);
					And and = new And();
					for (int x = 0; x < 9; x++) {
						if (x != j) {
							and.addOperand(new Not(Xijk[i][x][k]));
						}
					}
					implies.setLeft(Xijk[i][j][k]);
					implies.setRight(and);
					formulas.add(implies);
				}
				formulas.add(or);
			}
		}
	}

	private void createOneNumberPerCaseFormulas() {
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				Or or = new Or();

				for (int k = 0; k < 9; k++) {
					Implies implies = new Implies(null, null);
					or.addOperand(Xijk[i][j][k]);
					And and = new And();
					for (int x = 0; x < 9; x++) {
						if (x != k) {
							and.addOperand(new Not(Xijk[i][j][x]));
						}
					}
					implies.setLeft(Xijk[i][j][k]);
					implies.setRight(and);
					formulas.add(implies);
				}
				formulas.add(or);
			}
		}
	}

	private void initializePropositionalVariables() {
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				for (int k = 0; k < 9; k++) {
					Xijk[i][j][k] = new PropositionalVariable("X" + i + j + k);
				}
			}
		}
	}

	private void fillCasesFromPuzzleString(String puzzleString) {
		int i = 0;
		int j = 0;
		for (int charIndex = 0; charIndex < puzzleString.length(); charIndex++) {

			if (puzzleString.charAt(charIndex) != '#') {
				cases[i][j] = Character.getNumericValue(puzzleString.charAt(charIndex)) - 1;
			} else {
				cases[i][j] = -1;
			}

			j++;
			if (charIndex % 9 == 8) {
				i++;
				j = 0;
			}
		}
	}
}
