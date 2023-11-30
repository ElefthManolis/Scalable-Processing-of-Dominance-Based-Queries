import os
import argparse
import matplotlib.pyplot as plt
import numpy as np


def parse_args():
    parser = argparse.ArgumentParser(add_help=True)
    parser.add_argument('--dimensions', type=int, required=True,
                    help='an integer for the data dimensions')
    parser.add_argument('--num_points', type=int, required=True,
                    help='an integer for the number of points')
    parser.add_argument("--show", action="store_true", 
                        help="plot the dataset") 
    args = parser.parse_args() 
    return args



def correlated_covariance_matrix(dim):
    covariance_matrix = np.random.rand(dim, dim)
    covariance_matrix = np.dot(covariance_matrix, covariance_matrix.T)
    return covariance_matrix


def anticorrelated_covariance_matrix(dim):
    covariance_matrix = np.random.rand(dim, dim)
    covariance_matrix = np.dot(covariance_matrix, covariance_matrix.T)
    for i in range(dim):
        covariance_matrix[i, i] = -abs(covariance_matrix[i, i])
    return covariance_matrix


def show_distribution(uniform, normal, correlated, anticorrelated):
    fig, axs = plt.subplots(2, 2)
    axs[0, 0].scatter(uniform[:, 0], uniform[:, 1])
    axs[0, 0].set_title('Uniform Distribution')
    axs[0, 1].scatter(normal[:, 0], normal[:, 1])
    axs[0, 1].set_title('Normal Distribution')
    axs[1, 0].scatter(correlated[:, 0], correlated[:, 1])
    axs[1, 0].set_title('Correlated Distribution')
    axs[1, 1].scatter(anticorrelated[:, 0], anticorrelated[:, 1])
    axs[1, 1].set_title('Anticorrelated Distribution')
    plt.show()



def save_datasets(uniform, normal, correlated, anticorrelated, dim, num_of_points):
    current_directory = os.getcwd()
    f_normal = open(current_directory + '/datasets/' + str(dim) + '_' + str(num_of_points) + '_normal.txt', 'w')
    f_uniform = open(current_directory + '/datasets/' + str(dim) + '_' + str(num_of_points) + '_uniform.txt', 'w')
    f_correlated = open(current_directory + '/datasets/' + str(dim) + '_' + str(num_of_points) + '_correlated.txt', 'w')
    f_anticorrelated = open(current_directory + '/datasets/' + str(dim) + '_' + str(num_of_points) + '_anticorrelated.txt', 'w')

    for i in range(num_of_points):
        f_normal.write(' '.join(str(e) for e in normal[i]) + '\n')
        f_uniform.write(' '.join(str(e) for e in uniform[i]) + '\n')
        f_correlated.write(' '.join(str(e) for e in correlated[i]) + '\n')
        f_anticorrelated.write(' '.join(str(e) for e in anticorrelated[i]) + '\n')

    f_normal.close()
    f_uniform.close()
    f_correlated.close()
    f_anticorrelated.close()



def main():
    args = parse_args()
    
    dataset_uniform = []
    dataset_normal = []

    # mean numpy array
    mean = np.zeros(args.dimensions)

    # covariance matricies in N dimensions
    cov_anticorrelated = anticorrelated_covariance_matrix(args.dimensions)
    cov_correlated = correlated_covariance_matrix(args.dimensions)
    
    
    dataset_anticorrelated = np.random.multivariate_normal(mean, cov_anticorrelated, args.num_points)
    dataset_correlated = np.random.multivariate_normal(mean, cov_correlated, args.num_points)

    for i in range(args.num_points):
        dataset_normal.append(list(np.random.normal((100, 100))))
        dataset_uniform.append(list(np.random.uniform((100, 100))))

    save_datasets(dataset_uniform, dataset_normal, dataset_correlated, dataset_anticorrelated, args.dimensions, args.num_points)

    if args.show and (args.dimensions == 2):
        show_distribution(np.array(dataset_uniform), np.array(dataset_normal), dataset_correlated, dataset_anticorrelated)

    


if __name__ == '__main__':
    main()