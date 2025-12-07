import pickle
import numpy as np
from matplotlib import pyplot as plt
import scipy.stats
import warnings
from matplotlib.lines import Line2D
from matplotlib.legend import Legend
import os
from exp_params import ExpParams




if __name__ == "__main__":

    plots_path = 'plots'
    if not os.path.exists(plots_path):
        os.makedirs(plots_path)
    with open('manager_data5.pkl', 'rb') as f:
        manager = pickle.load(f)

    nkw=500
    pct_adv=50

    styles = ['-', '--']
    colors = ['C0', 'C1', 'C2', 'C3', 'C4']
    title_list = ['ddr-sse']
    def_list = [('none', ()), ('clrz', (0.999, 0.1)), ('ddr-sse', (400,))]
    nkw_list = [100, 500, 1000]

    results_to_plot_accuracy = []

    
    # For boxplots
    yvalues = []
    ydumbvals = []
    yalpha1vals = []
    bwvals = []
    xvalues = []
    xlabels = []
    colors = []
    for i_def, (def_name, def_params) in enumerate(def_list):
        current_y_mean = -1
        candidate_y_vals = []
        mean_y_alpha1 = -1
                        
        exp_params = ExpParams()
        if def_name == 'none':
            exp_params.set_defense_params(def_name)
        elif def_name == 'clrz':
            exp_params.set_defense_params(def_name, tpr=def_params[0], fpr=def_params[1])
        elif def_name == 'ddr-sse':
            exp_params.set_defense_params(def_name, bkt=def_params[0])

        exp_params.set_attack_params('ihop', **{'mode': 'Vol', 'niters': 10000, 'pfree': 0.25})
            
        for i_nkw, nkw in enumerate(nkw_list):
            exp_params.set_general_params(dataset='enron-full', nkw=nkw, nqr=nkw, freq='none', mode_ds='split{:d}'.format(pct_adv), mode_fs='past', mode_kw='rand', mode_query='each')
            result = manager.get_results(exp_params)
            accuracy_vals = result[0]

            if len(accuracy_vals) > 0:
                if np.mean(accuracy_vals) > current_y_mean:
                    current_y_mean = np.mean(accuracy_vals)
                    candidate_y_vals = accuracy_vals


            xvalues.append(i_def * (len(nkw_list) + 1) + i_nkw)
            xlabels.append(str(def_params) + ' ' + str(nkw))
            yvalues.append(candidate_y_vals)
            colors.append('C{:d}'.format(i_nkw))

    fig, ax1 = plt.subplots(figsize=(6, 4))
    box = ax1.boxplot(yvalues, positions=xvalues, patch_artist=True)
    for patch, color in zip(box['boxes'], colors):
        patch.set_facecolor(color)
    for item in ['whiskers', 'fliers', 'medians', 'caps']:
        plt.setp(box[item], color='k')
    legend_elements = box['boxes'][:len(nkw_list)]
    legend_labels = ['$n$={:d}'.format(nkw) for nkw in nkw_list]
    legend1 = Legend(ax1, legend_elements, legend_labels, title='$\\mathtt{IHOP}$ accuracy', loc='upper right')
    ax1.add_artist(legend1)

    xtick_positions = []
    for i in range(len(def_list)):
        mid_pos = 0.5 * (xvalues[i*len(nkw_list)] + xvalues[(i+1)*len(nkw_list) - 1])
        xtick_positions.append(mid_pos)
        #ax1.plot(xvalues[i*len(nkw_list):(i+1)*len(nkw_list)], ydumbvals[i*len(nkw_list):(i+1)*len(nkw_list)], 'kv:')
        #ax1.plot(xvalues[i*len(nkw_list):(i+1)*len(nkw_list)], yalpha1vals[i*len(nkw_list):(i+1)*len(nkw_list)], 'bv:')
    xtick_labels = ['no defense', 'CLRZ, $\\mathtt{FPR}=0.1$', 'DDR-SSE, bkt=400' ]
    plt.xticks(xtick_positions, xtick_labels, fontsize=12)
    ax1.set_ylim([-0.01, 1.01])
    ax1.set_ylabel('Attack Accuracy', fontsize=12)

    plt.savefig(plots_path + '/IHOP-ddr-sse.pdf')
    plt.show()



            
            
