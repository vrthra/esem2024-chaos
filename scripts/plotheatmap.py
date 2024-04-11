import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
plt.rcParams['font.family'] = 'monospace'

import matplotlib
import matplotlib.cm as cm
from matplotlib.patches import Rectangle

import itertools

def get_iou(pred_box, gt_box):
    """
    pred_box : the coordinate for predict bounding box
    gt_box :   the coordinate for ground truth bounding box
    return :   the iou score
    the  left-down coordinate of  pred_box:(pred_box[0], pred_box[1])
    the  right-up coordinate of  pred_box:(pred_box[2], pred_box[3])
    """
    # 1.get the coordinate of inters
    ixmin = max(pred_box[0], gt_box[0])
    ixmax = min(pred_box[2], gt_box[2])
    iymin = max(pred_box[1], gt_box[1])
    iymax = min(pred_box[3], gt_box[3])

    iw = np.maximum(ixmax-ixmin+1., 0.)
    ih = np.maximum(iymax-iymin+1., 0.)

    # 2. calculate the area of inters
    inters = iw*ih

    # 3. calculate the area of union
    uni = ((pred_box[2]-pred_box[0]+1.) * (pred_box[3]-pred_box[1]+1.) +
           (gt_box[2] - gt_box[0] + 1.) * (gt_box[3] - gt_box[1] + 1.) -
           inters)

    # 4. calculate the overlaps between pred_box and gt_box
    iou = inters / uni

    if iou < 0.0 or iou > 1.0:
        pass

    return iou


def get_box_for(df, subject, sampling, estimator, test_suite):
    filter_condition = (df["subject"] == subject) & (df["estimator"] == estimator) & (df["type"] == sampling) & (df["testsuite"] == test_suite)
    min_value = df[filter_condition]['CILower'].values[0]
    max_value = df[filter_condition]['CIUpper'].values[0]
    return (min_value, 1, max_value, 2)


def get_estimate(df, subject, sampling, estimator, test_suite):
    filter_condition = (df["subject"] == subject) & (df["estimator"] == estimator) & (df["type"] == sampling) & (df["testsuite"] == test_suite)
    min_value = df[filter_condition]['CILower'].values[0]
    mean_value = df[filter_condition]['estimation'].values[0]
    max_value = df[filter_condition]['CIUpper'].values[0]
    return min_value, mean_value, max_value

def get_gt(df, subject):
    filter_condition = (df["subject"] == subject)
    min_value = df[filter_condition]['MLower'].values[0]
    mean_value = df[filter_condition]['MEstimate'].values[0]
    max_value = df[filter_condition]['MUpper'].values[0]
    return min_value, mean_value, max_value


def heatmap(data, row_labels, col_labels, ax=None,
            cbar_kw=None, cbarlabel="", **kwargs):
    """
    Create a heatmap from a numpy array and two lists of labels.

    Parameters
    ----------
    data
        A 2D numpy array of shape (M, N).
    row_labels
        A list or array of length M with the labels for the rows.
    col_labels
        A list or array of length N with the labels for the columns.
    ax
        A `matplotlib.axes.Axes` instance to which the heatmap is plotted.  If
        not provided, use current axes or create a new one.  Optional.
    cbar_kw
        A dictionary with arguments to `matplotlib.Figure.colorbar`.  Optional.
    cbarlabel
        The label for the colorbar.  Optional.
    **kwargs
        All other arguments are forwarded to `imshow`.
    """

    if ax is None:
        ax = plt.gca()

    if cbar_kw is None:
        cbar_kw = {}

    # Plot the heatmap
    im = ax.imshow(data, **kwargs)

    # Create colorbar
    # cbar = ax.figure.colorbar(im, ax=ax, **cbar_kw)
    # cbar.ax.set_ylabel(cbarlabel, rotation=-90, va="bottom")

    # Show all ticks and label them with the respective list entries.
    ax.set_xticks(np.arange(len(col_labels)), labels=col_labels)
    ax.set_yticks(np.arange(len(row_labels)), labels=row_labels)

    # Let the horizontal axes labeling appear on top.
    ax.tick_params(top=True, bottom=False,
                   labeltop=True, labelbottom=False)

    # Rotate the tick labels and set their alignment.
    plt.setp(ax.get_xticklabels(), rotation=-30, ha="right",
             rotation_mode="anchor")

    # Turn spines off and create white grid.
    ax.spines[:].set_visible(False)

    ax.set_xticks(np.arange(len(col_labels)+1)-.5, minor=True)
    ax.set_yticks(np.arange(len(row_labels)+1)-.5, minor=True)
    ax.grid(which="minor", color="w", linestyle='-', linewidth=3)
    ax.tick_params(which="minor", bottom=False, left=False)

    return im


def annotate_heatmap(im, data=None, valfmt="{x:.2f}",
                     textcolors=("black", "white"),
                     threshold=None, **textkw):
    """
    A function to annotate a heatmap.

    Parameters
    ----------
    im
        The AxesImage to be labeled.
    data
        Data used to annotate.  If None, the image's data is used.  Optional.
    valfmt
        The format of the annotations inside the heatmap.  This should either
        use the string format method, e.g. "$ {x:.2f}", or be a
        `matplotlib.ticker.Formatter`.  Optional.
    textcolors
        A pair of colors.  The first is used for values below a threshold,
        the second for those above.  Optional.
    threshold
        Value in data units according to which the colors from textcolors are
        applied.  If None (the default) uses the middle of the colormap as
        separation.  Optional.
    **kwargs
        All other arguments are forwarded to each call to `text` used to create
        the text labels.
    """

    if not isinstance(data, (list, np.ndarray)):
        data = im.get_array()

    # Normalize the threshold to the images color range.
    if threshold is not None:
        threshold = im.norm(threshold)
    else:
        threshold = im.norm(data.max())/2.

    # Set default alignment to center, but allow it to be
    # overwritten by textkw.
    kw = dict(horizontalalignment="center",
              verticalalignment="center")
    kw.update(textkw)

    # Get the formatter in case a string is supplied
    if isinstance(valfmt, str):
        valfmt = matplotlib.ticker.StrMethodFormatter(valfmt)

    # Loop over the data and create a `Text` for each "pixel".
    # Change the text's color depending on the data.
    texts = []
    for i in range(data.shape[0]):
        for j in range(data.shape[1]):
            kw.update(color=textcolors[int(im.norm(data[i, j]) > threshold)])
            text = im.axes.text(j, i, valfmt(data[i, j], None), **kw)
            texts.append(text)

    return texts


def plot_the_heatmap(summary_csv_file):
    # Read summary.csv 
    df = pd.read_csv(summary_csv_file)
    # Extract the data
    
    subjects = df["subject"].unique().tolist()

    estimators = df["estimator"].unique().tolist()
    max_e_len = max([len(e) for e in estimators])

    samplings = df["type"].unique().tolist()
    max_s_len = max([len(s) for s in samplings])

    test_suites = df["testsuite"].unique().tolist()
    max_ts_len = max([len(ts) for ts in test_suites])
    
    # Create one table for each subject
    headers_x = []
    headers_y = []
    # {0:>}
    string_format = "{0:>" + str(max_s_len) + "} {1:>" + str(max_e_len) + "}"


    for subject in subjects:
        print(f"Subject {subject} expecting a table {len(list(itertools.product(samplings, estimators, test_suites)))} x {len(list(itertools.product(samplings, estimators, test_suites)))}")
        # Create the headers
        
        data = []
        # computed = []
        
        pos_x = 0

        for s1, e1, ts1 in list(itertools.product(samplings, estimators, test_suites)):
            box1 = get_box_for(df, subject=subject, sampling=s1, estimator=e1, test_suite=ts1)
            
            sampling_header = ""
            if pos_x == (len(estimators) * len(samplings) * len(test_suites)) / 4 and pos_x > 0:
                sampling_header = f"{s1}"

            if pos_x == (len(estimators) * len(samplings) * len(test_suites)) * 3 / 4 and pos_x > 0:
                sampling_header = f"{s1}"
            
            estimator_header = ""
            if pos_x % 3 == 1 and pos_x > 0:
                estimator_header = f"{e1}"

            the_header_x = string_format.format(sampling_header, estimator_header)

            headers_x.append(the_header_x)

            row = []
            headers_y = []

            pos_y = 0

            for s2, e2, ts2 in list(itertools.product(samplings, estimators, test_suites)):

                sampling_header = ""
                if pos_y == (len(estimators) * len(samplings) * len(test_suites)) / 4 and pos_y > 0:
                    sampling_header = f"{s2}"

                if pos_y == (len(estimators) * len(samplings) * len(test_suites)) * 3 / 4 and pos_y > 0:
                    sampling_header = f"{s2}"
                
                estimator_header = ""
                if pos_y % 3 == 1 and pos_y > 0:
                    estimator_header = f"{e2}"

                the_header_y = string_format.format(sampling_header, estimator_header)

                headers_y.append(the_header_y)

                box2 = get_box_for(df, subject=subject, sampling=s2, estimator=e2, test_suite=ts2)
                # iou = IOU(box1, box2)
                iou = get_iou(box1, box2)
                

                print(f"Comparing {e1}-{s1}-{ts1} against {e2}-{s2}-{ts2} = {iou}")

                row.append(iou)

                pos_y = pos_y + 1
            
            pos_x = pos_x + 1

            data.append(row)

            # We have done the first group
            # computed.append(f"{e1}-{s1}-{ts1}")

        # Adjust the headers such that, all of them have the same length, which is the maximum length
        # The one at position 1/4 gets the Class/Method
        # Every 3 elements starting from the second get the Prediction name
        # No Test Suite



        fig, ax = plt.subplots(figsize=(15,15))
        
        fig.subplots_adjust(left=0.3)

        # TODO Remove the bar
        im = heatmap(data, headers_x, headers_y, ax=ax,
                   cmap=cm.binary, cbarlabel="IoU/Overlapping")
        # texts = annotate_heatmap(im, valfmt="{x:.1f} t")

        # Add the "grid lines" to easily locate the predictors
        max_max = (len(estimators) * len(samplings) * len(test_suites))
        for _x in np.arange(0, max_max + 3, 3):
            # Move the line a little up and left
            x = _x - 0.5
            ax.plot([x, x], [-0.5, max_max - 0.5], "-", color="black", linewidth=0.5, alpha=0.5)
            ax.plot([-0.5, max_max - 0.5], [x, x], "-", color="black", linewidth=0.5, alpha=0.5)

        ax.set_xlim([-0.5, max_max])
        # Heatmap is rotated
        ax.set_ylim([max_max, -0.5])

        ax.set_title(f"Estimators Agremeent for Project: {subject}")

        # fig.tight_layout()
        plt.show()

        break


def main(summary_csv_file):
    """
    Normalize
    """
    # Read summary.csv 
    df = pd.read_csv(summary_csv_file)
    # Extract the data
    
    subjects = df["subject"].unique().tolist()

    estimators = df["estimator"].unique().tolist()
    max_e_len = max([len(e) for e in estimators])

    samplings = df["type"].unique().tolist()
    max_s_len = max([len(s) for s in samplings])

    test_suites = df["testsuite"].unique().tolist()
    max_ts_len = max([len(ts) for ts in test_suites])

    MAX = len(estimators) * len(samplings) * len(test_suites)
    LIMIT = 20
    PLOT_LIMT = 4 * LIMIT

    string_format = "{0:>" + str(max_s_len) + "} {1:>" + str(max_e_len) + "}"

    # Horizontal plot: For each project show the bars corresponding to the CI of the various estimators wrt the ML
    for subject in subjects:
        print(f"Subject {subject} expecting a table {len(list(itertools.product(samplings, estimators, test_suites)))} x {len(list(itertools.product(samplings, estimators, test_suites)))}")
        # Create the headers        
        # for s1, e1, ts1 in list(itertools.product(samplings, estimators, test_suites)):
        
        fig, ax = plt.subplots(figsize=(20,5))
        low, middle, high = get_gt(df, subject)
        
        translation = low

        # Translate to y=0
        high = high - translation
        middle = middle - translation
        low = low - translation
        
        # Normalize to 0 to LIMIT
        normalization_factor = LIMIT / high
        
        high = high * normalization_factor
        middle = middle * normalization_factor
        low = low * normalization_factor

        # Draw a large line simulating a bar
        mp = (low + high) * 0.5
        ax.plot([0, MAX], [mp, mp], linewidth=40, color="black", alpha=0.2)

        ax.plot([0, MAX], [low, low], color="black")
        ax.plot([0, MAX], [middle, middle], "--", color="red")
        ax.plot([0, MAX], [high, high], color="black")
        
        ax.set_ylim([-4 * LIMIT, 4 * LIMIT])
        ax.set_yticks([middle], ["Point Estimate"])

        import numpy as np
        import matplotlib.pyplot as pl

        n = int(len(list(itertools.product(samplings, estimators, test_suites))) / 2 + 1)
        colors = pl.cm.jet(np.linspace(0,1,n))

        labels = []
        # Normalize the data in the same way, and plot them as vertical bars positioned at index, and large 10
        for position, (s, e, ts) in enumerate(list(itertools.product(samplings, estimators, test_suites))):
            color = colors[position % n]
            # Make color darker instead
            delta_alpha = 0 if position < len(list(itertools.product(samplings, estimators, test_suites))) * 0.5 else 0.4
            
            sampling_header = ""
            # if position == (len(estimators) * len(samplings) * len(test_suites)) / 4 and position > 0:
            #     sampling_header = f"{s}"

            # if position == (len(estimators) * len(samplings) * len(test_suites)) * 3 / 4 and position > 0:
            #     sampling_header = f"{s}"
                
            estimator_header = ""
            if position % 3 == 1 and position > 0:
                estimator_header = f"{e}"

            the_label = string_format.format(sampling_header, estimator_header)

            labels.append(the_label)

            low_e, middle_e, high_e = get_estimate(df, subject, s, e, ts)

            low_e = (low_e - translation) * normalization_factor
            middle_e = (middle_e - translation) * normalization_factor
            high_e = (high_e - translation) * normalization_factor

            # Plot the mean value and get the color
            ax.plot([position-0.5, position+0.5], [middle_e, middle_e], color="black", linewidth=2)
                      
            # Plot the markers if necessary
            if high_e < - PLOT_LIMT:
                ax.plot([position], [-PLOT_LIMT + 5], marker='v', color=color, markersize=10, alpha = 1 - delta_alpha)
                continue
            elif low_e > PLOT_LIMT:
                ax.plot([position], [PLOT_LIMT - 5], marker='^', color=color, markersize=10, alpha = 1 - delta_alpha)
                continue

            # Partially inside            
            if high_e > PLOT_LIMT:
                # Truncate
                ax.plot([position], [PLOT_LIMT - 5], marker='^', color=color, markersize=10, alpha = 1 - delta_alpha)

            if low_e < - PLOT_LIMT:
                ax.plot([position], [-PLOT_LIMT + 5], marker='v', color=color, markersize=10, alpha = 1 - delta_alpha)

            # Plot the truncated bar
            # Truncate low_e and high_e
            trunc_low_e = -PLOT_LIMT + 12 if low_e < - PLOT_LIMT else low_e
            trunc_high_e = PLOT_LIMT - 12 if high_e > PLOT_LIMT else high_e

            ax.plot([position, position], [trunc_low_e, trunc_high_e], color=color, linewidth=10, alpha=0.5 - delta_alpha)


        # Set the limits to be - 2 * LIMIT and 2 * LIMIT
        ax.set_xlim([-1, len(list(itertools.product(samplings, estimators, test_suites)))])
        ax.set_xticks(np.arange(len(labels)), labels=labels)

        # plt.xticks(np.arange(-math.pi,math.pi+1,math.pi), [r'$-\pi$', r'$0$', r'$\pi$' ])
        # Rotate the tick labels and set their alignment.
        plt.setp(ax.get_xticklabels(), rotation=30, ha="right", rotation_mode="anchor")

        # Add the "grid lines" to easily locate the predictors
        max_max = (len(estimators) * len(samplings) * len(test_suites))
        for _x in np.arange(0, max_max + 3, 3):
            # Move the line a little left
            x = _x - 0.5
            ax.plot([x, x], [-PLOT_LIMT, PLOT_LIMT], "-", color="black", linewidth=0.5, alpha=0.5)

        fig.tight_layout()
        plt.show()

        break

if __name__ == "__main__":
    # Get the location of this script"
    import os
    scripts_dir = os.path.dirname(os.path.realpath(__file__))
    summary_csv_file = os.path.join(scripts_dir, "summary.csv")

    # plot_the_heatmap(summary_csv_file)
    main(summary_csv_file)
