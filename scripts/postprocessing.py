import argparse
from pathlib import Path

import pandas as pd
import plotnine
from mizani.formatters import percent_format
from plotnine import *
from statsmodels.stats.proportion import proportion_confint

estimator_map = {'ice': 'ICE_k0',
                 'zelterman': 'Zelterman',
                 'species_chaobunge': 'Chao_Bunge',
                 'species_jackknife': 'Jackknife',
                 'spader_chao': 'Chao',
                 'spader_chao2bc': 'Chao-bc',
                 'spader_ichao2': 'iChao',
                 'spader_ice': 'ICE',
                 'spader_ice1': 'ICE-1',
                 'species_pcg': 'PCG',
                 'bootstrap': 'Bootstrap',
                 'species_unpmle': 'Unpmle',
                 'species_pnpmle': 'Pnpmle',
                 'chao_upperbound': 'Chao-up',
                 'chao_lower': 'Chao-low',
                 'sample_coverage': 'ICE-k0'}
subject_map = {'commons-collections': "10",
               'commons-compress': "9",
               'commons-configuration': "8",
               'commons-csv': "7",
               'commons-dbcp': "6",
               'commons-imaging': "5",
               'commons-io': "4",
               'commons-lang': "3",
               'commons-math': "2",
               'commons-net': "1"}
catch_type = ['methods', 'classes']
testsuites = ['organic', 'random', 'dynamosa']


def apply_threshold(df, threshold=3):
    """
    exclude estimations that are too high
    """
    return df[df.estimation < df.total_mutants * threshold]


def load_table(summary_path, ctype, estimators=None, numbered_subject=False):
    df = pd.read_csv(summary_path)
    df = df.sort_values(by=["subject", "estimator"])
    df = df[df.type.isin(ctype)]
    # remove negative values if any
    df.fillna(0, inplace=True)
    df = df[df.estimation > 0]
    df = df[df.estimation < 1e9]

    df = apply_threshold(df)
    if estimators is not None:
        df = df[df.estimator.isin(estimators)]
    df['estimator'] = df.estimator.apply(lambda x: estimator_map[x])
    if numbered_subject:
        df['subject'] = df.subject.apply(lambda x: subject_map[x])
    df["err_minus"] = df["estimation"] - df["CILower"]
    df["err_plus"] = df["CIUpper"] - df["estimation"]
    return df


def ggplot_subject_testsuites(summary_path, chart_path, w=6.8, h=4.8, numbered_subject=False):
    df = load_table(summary_path, catch_type, numbered_subject=numbered_subject)
    chart_path.mkdir(exist_ok=True)
    for subject in df.subject.unique():
        for testsuite in df.testsuite.unique():
            print(subject, testsuite)
            ggplot_subject_testsuite(df, subject, testsuite, path=chart_path, w=w, h=h)


def ggplot_subject_testsuite(data, subject, testsuite, path=None, w=6.8, h=4.8):
    color_discrete_map = {'methods': '#d62728', 'classes': '#1f77b4'}
    df = data[(data.subject == subject) & (data.testsuite == testsuite)]
    if df.size == 0:
        print("NO DATA")
        return
    total = df.total_mutants.max()
    max_value = df.CIUpper.max()
    ylim_max = None if 2 * total > max_value else 2 * total
    ylim_min = min(df.CILower.min(), df.killed.min()) - 100  # 0
    num_est = df.estimator.size
    ch = (ggplot(df) +
          aes(x='estimator', y='estimation', color='type', shape='type') +
          coord_cartesian() +
          geom_point(size=2, position=position_dodge(width=0.5)) +
          geom_errorbar(aes(ymin='CILower', ymax='CIUpper', color='type'), size=1, width=0.6, position=position_dodge(width=0.5)) +
          scale_color_manual(color_discrete_map) +
          geom_hline(yintercept=df.total_mutants) +
          geom_hline(yintercept=df.killed, linetype='dotted') +
          geom_hline(yintercept=df.MEstimate, linetype='dashed', color='black') +
          geom_hline(yintercept=df.MUpper, linetype='dashed', color='gray') +
          geom_hline(yintercept=df.MLower, linetype='dashed', color='gray') +
          geom_vline(xintercept=list(map(0.5.__add__, range(1, num_est, 1))), color="lightgray") +
          coord_flip(ylim=[ylim_min, ylim_max]) +
          facet_wrap('subject') +
          theme_light() +
          theme(strip_background=element_rect(fill="gray", alpha=0),
                text=element_text(colour="black")
                ) +
          theme(
              axis_text_y=element_text(size=16),  # element_blank(),
              axis_text_x=element_text(size=10),
              text=element_text(size=18)) +
          theme(legend_position="none") +
          theme(
              axis_line=element_blank(),
              axis_title_x=element_blank(),
              axis_title_y=element_blank(),
              panel_grid=element_blank(),
              panel_grid_major_x=element_line(color='lightgray'),
              panel_grid_minor_x=element_line(color='lightgray'),
              panel_ontop=False
          )
          )
    plotnine.options.figure_size = (w, h)
    if path is not None:
        ch.save(path / f'{subject}_{testsuite}.pdf', width=w, height=h, verbose=True)
    else:
        print(ch)


def ggplot_estimators(summary_path, chart_path, w=6.8, h=4.8, numbered_subject=False, norm_by_mest=False):
    df = load_table(summary_path, catch_type, numbered_subject=numbered_subject)
    print(chart_path)
    chart_path.mkdir(exist_ok=True)
    for estimator in df.estimator.unique():
        print(estimator)
        ggplot_estimate(df, estimator, path=chart_path, w=w, h=h, norm_by_mest=norm_by_mest)


def ggplot_estimate(data, estimator, w=6.8, h=4.8, path=None, norm_by_mest=False):
    df = data[(data.estimator == estimator)].copy()
    if df.size == 0:
        print("NO DATA")
        return
    df = manual_to_row(df, estimator, norm_by_mest)
    color_map = {"organic": '#d62728', "random": '#1f77b4', "dynamosa": '#2ca02c', 'manual': '#9467bd', 'merged': '#937023'}
    df['tt'] = df['testsuite'] + ":" + df['type']
    shape_map = {"organic:methods": 'o', "random:methods": 'd', "dynamosa:methods": 's', 'manual:manual': '|',
                 "organic:classes": 'o', "random:classes": 'd', "dynamosa:classes": 's', "merged:methods": 's'}
    fill_map = {"organic:methods": '#d62728', "random:methods": '#1f77b4', "dynamosa:methods": '#2ca02c', 'manual:manual': '#9467bd',
                "organic:classes": 'white', "random:classes": 'white', "dynamosa:classes": 'white',
                "merged:methods": '#937023'}
    linetype_map = {'methods': 'solid', 'classes': 'dotted', 'manual': 'solid'}
    num_subj = df.subject.size
    print(df['rmax'].max())
    ym = min(2, df['rmax'].max())
    y_max = max(1.1, ym)
    ch = (ggplot(df) +
          aes(x='subject', y='rest', color='testsuite', shape='tt', fill='tt') +
          geom_point(size=1.5, position=position_dodge(width=0.7)) +
          scale_y_continuous(labels=percent_format()) +

          scale_x_discrete(limits=df.subject.unique()) +
          geom_errorbar(aes(ymin='rmin', ymax='rmax', color='testsuite', linetype='type'), size=1, width=2,
                        position=position_dodge(width=0.7)) +
          scale_color_manual(values=color_map) +
          scale_shape_manual(values=shape_map) +
          scale_fill_manual(values=fill_map) +
          scale_linetype_manual(values=linetype_map) +
          geom_hline(yintercept=1, linetype='solid', color='black', size=0.25) +
          geom_vline(xintercept=list(map(0.5.__add__, range(1, num_subj, 1))), color="lightgray") +
          coord_flip(ylim=[0.2, y_max]) +
          #          facet_wrap('estimator') +
          theme_light() +
          # theme(strip_background=element_rect(fill="gray", alpha=0),
          #      text=element_text(colour="black")
          #      ) +
          theme(
              axis_text_y=element_text(size=16),  # element_blank(),
              axis_text_x=element_text(size=10),
              text=element_text(size=18)) +
          theme(legend_position="none") +
          theme(
              axis_line=element_blank(),
              axis_title_x=element_blank(),
              axis_title_y=element_blank(),
              panel_grid=element_line(color='white'),
              panel_grid_major_x=element_line(color='lightgray'),
              panel_grid_minor_x=element_line(color='lightgray'),
              panel_ontop=False
          )

          )
    plotnine.options.figure_size = (w, h)
    if path is not None:
        path.mkdir(parents=True, exist_ok=True)
        ch.save(path / f"{estimator}.pdf", width=w, height=h, verbose=False)
    else:
        print(ch)  # for jupyter


def manual_to_row(df, estimator, norm_by_mest):
    norm = 'MEstimate' if norm_by_mest else 'total_mutants'
    # add manual
    subjects = df.subject.unique()
    for subject in subjects:
        row = df[df.subject == subject].iloc[0]
        value = row['MEstimate']
        val_min = value - row['MLower']
        val_plus = row['MUpper'] - value
        mdf = pd.DataFrame([{'subject': subject, 'testsuite': 'manual', 'type': 'manual', 'total_mutants': row['total_mutants'],
                             'killed': row['killed'], 'estimator': estimator, 'estimation': value, 'CILower': row['MLower'], 'CIUpper': row['MUpper'],
                             'MEstimate': 0, 'MLower': 0, 'MUpper': 0,
                             'err_minus': val_min, 'err_plus': val_plus}])
        df = pd.concat([df, mdf])
    df['rest'] = df['estimation'] / df[norm]
    df['rmin'] = df['CILower'] / df[norm]
    df['rmax'] = df['CIUpper'] / df[norm]
    return df


def mean_difference(summary, value, column, by_type=None, valid=False):
    df = summary[summary[column] == value]
    df = df.dropna()
    if by_type is not None:
        df = df[df.type == by_type]
    df = df[df.estimation > 0]
    if valid:
        df = df[df.estimation <= df.total_mutants]
    # df = apply_threshold(df)
    # if absolute:
    difference = abs(df['estimation'] - df['MEstimate'])
    difference_n2 = abs(df['estimation'] - df['MEstimate']) / df['total_mutants']
    difference2 = (df['estimation'] - df['MEstimate']) ** 2
    # else:
    difference_n = abs(df['estimation'] - df['MEstimate']) / df['MEstimate']
    mae = difference.mean()
    mean = difference.mean()
    std = difference.std()
    std_n = difference_n.std()
    mse = difference2.mean()
    rsme = mse ** 0.5
    std_total = difference_n2.std() * 100
    mae_total = difference_n2.mean() * 100
    mape = difference_n.mean() * 100
    return {column: value, 'mae': mae, 'mae_total': mae_total, 'mape': mape, 'mse': mse, 'rsme': rsme,
            'std': std, 'std_total': std_total, 'stdp': std_n * 100, 'nValid': len(difference)}


def mean_diff_estimator(summary, by_type=None, valid=False):
    all_mean = []
    for estimator in summary.estimator.unique():
        all_mean.append(mean_difference(summary, estimator, 'estimator', by_type, valid))
    df = pd.DataFrame(all_mean)
    df = df.round(2)
    return df


def get_mean_difference(summary_path, out_file, testsuite, by_type=None, valid=False):
    summary = load_table(summary_path, catch_type)
    summary = summary[summary.testsuite == testsuite]
    res = mean_diff_estimator(summary, by_type, valid)
    res.to_csv(out_file, index=False)


def load_classified(c_path, subject):
    df = pd.read_csv(c_path / f"{subject}.sample.rb.csv", sep='`')
    head = df.columns[0].split(',')
    df = df.iloc[:, 0].str.split(',', expand=True, n=9).apply(lambda x: x.str.strip())
    df.columns = head
    df.index = pd.to_numeric(df.Mutant)
    if (c_path / f"{subject}.sample.decision").stat().st_size > 0:
        decision = pd.read_csv(c_path / f"{subject}.sample.decision", header=None)
        decision = decision.replace({"^\s*|\s*$": ""}, regex=True)  # remove spaces
        decision.columns = ['Mutant', 'Equivalent']
        decision.index = pd.to_numeric(decision.Mutant)
        decision = decision.Equivalent
        df.update(decision)
    df = df[['File', 'Line', 'Index', 'Equivalent', 'Mutator']]
    df['Equivalent'] = df['Equivalent'].str.strip()
    df['Mutator'] = 'org.pitest.mutationtest.engine.gregor.mutators.' + df['Mutator']
    df['File'] = df['File'].str.split('.').str[0]
    df['File'] = df['File'].str.replace('/', '.')
    df.columns = ['mutatedClassTrim', 'lineNumber', 'index', 'equivalent', 'mutator']
    return df


def manual_estimation(non_equiv_count, classified_count, killed, total_mutants):
    confint = proportion_confint(non_equiv_count, classified_count, method="beta")
    mean_val = non_equiv_count / classified_count
    m_estimate = killed + mean_val * (total_mutants - killed)
    m_ciupper = killed + confint[1] * (total_mutants - killed)
    m_cilower = killed + confint[0] * (total_mutants - killed)
    return m_estimate, m_cilower, m_ciupper


def get_manual_estimation(subject, killed, total_mutants, classified_path):
    df_classified = load_classified(classified_path, subject)
    classified_count = len(df_classified)
    equivalent_count = len(df_classified[df_classified.equivalent == "yes"])
    non_equivalent_count = classified_count - equivalent_count
    manual_est, manual_min, manual_max = manual_estimation(non_equivalent_count, classified_count, killed, total_mutants)
    return manual_est, manual_min, manual_max


def add_manual_estimation(summary_path, classified_path):
    summary = pd.read_csv(summary_path)
    ts = summary.testsuite.values[0]
    print(ts)
    manual_est = summary[summary.testsuite == ts][['subject', 'testsuite', 'total_mutants', 'killed']].drop_duplicates()
    manual_est[['MEstimate', 'MLower', 'MUpper']] = manual_est.apply(
        lambda row: get_manual_estimation(row['subject'], row['killed'], row['total_mutants'], classified_path), axis=1,
        result_type="expand")
    manual_est = manual_est[['subject', 'MEstimate', 'MLower', 'MUpper']]
    summary.drop(['MEstimate', 'MLower', 'MUpper'], axis=1, inplace=True, errors='ignore')
    summary = pd.merge(summary, manual_est, how="left", on=["subject"])
    summary = summary.sort_values(by=['subject', 'testsuite', 'type', 'estimator'])
    summary.to_csv(summary_path, index=False)


def get_latex_mean_tables(summary_path, out_file, valid_only=False):
    summary = pd.read_csv(summary_path)
    summary = summary.sort_values(by=["subject", "estimator"])
    # remove negative values if any
    summary.fillna(0, inplace=True)
    summary = summary[summary.estimation > 0]  # no negative
    summary = summary[summary.estimation < 1e9]  # no infinite
    threshold = 3#1.2  # no too big
    if valid_only:
        threshold = 1  # only estimators less than total number of mutants
    summary = summary[summary.estimation <= summary.total_mutants * threshold]
    with out_file.open('w') as fd:
        for testsuite in testsuites:
            for c_type in catch_type:
                table = get_latex_mean_table(summary, testsuite, c_type, mean_column='mae_total', std_column='std_total')
                fd.write(table)
                fd.write('\n')


def get_latex_mean_table(summary, testsuite, c_type, mean_column, std_column):
    sample = {"methods": "method", "classes": "class"}
    testsuites = {"organic": r"\original", "random": r"\EvosuiteRandom", "dynamosa": r"\EvosuiteDynamosa"}
    subjects = {"ice": r"\ICEallrare", "zelterman": r"\Zelterman", "species_chaobunge": r"\ChaoBunge",
                "species_jackknife": r"\Jackknife", "spader_chao": r"\Chao", "spader_ichao2": r"\improvedChao",
                "spader_ice": r"\ICE", "spader_ice1": r"\improvedICE", "species_unpmle": r"\Unpmle",
                "bootstrap": r"\Bootstrap", "species_pnpmle": r"\Pnpmle", "species_pcg": r"\PCG"}
    label = {"organic": "original", "random": "random", "dynamosa": "dynamosa", \
             "methods": "", "classes": "class"}

    sort_order = list(subjects.values())
    header = (r"\begin{table}[h]" + "\n" +
              r"\caption{Comparison of \emph{mean difference (MD)} between " + sample[c_type] +
              " estimators across subjects---" + testsuites[testsuite] + r" test suites}" + "\n" +
              r"\begin{tabular}{|l|r|r|r|r|r|}" + "\n" +
              r"\hline" + "\n" +
              r"Estimator & Valid Estimates & CI Overlaps & Mean & Stdev \\" + "\n" +  # check header
              "\hline" + "\n")
    footer = (r"\hline" + "\n" +
              r"\end{tabular}" + "\n" +
              r"\label{tbl:est" + label[testsuite] + label[c_type] + "}" + "\n" +
              r"\end{table}"+ "\n")
    content = ""
    summary = summary[(summary.testsuite == testsuite) & (summary.type == c_type)]
    # \ICEallrare	&	8	&	1	&61.1	&	65.1\ \
    # Estimator & Valid Estimates & CI Overlaps & Mean & Stdev
    summary['CIOverlap'] = ((summary.CIUpper >= summary.MLower) & (summary.CIUpper <= summary.MUpper)) | \
                           ((summary.CILower >= summary.MLower) & (summary.CILower <= summary.MUpper)) | \
                           ((summary.CILower >= summary.MLower) & (summary.CIUpper <= summary.MUpper)) | \
                           ((summary.CILower <= summary.MLower) & (summary.CIUpper >= summary.MUpper))
    CIOverlap = summary.groupby(by='estimator')['CIOverlap'].sum()
    mean_diff = mean_diff_estimator(summary, c_type)
    mean_diff = mean_diff.join(CIOverlap, on='estimator')
    mean_diff['estimator'] = mean_diff['estimator'].map(subjects)
    mean_diff = mean_diff[['estimator', 'nValid', 'CIOverlap', mean_column, std_column]]
    mean_diff = mean_diff.sort_values('estimator', key=lambda s: s.apply(sort_order.index), ignore_index=True)
    content = mean_diff.to_csv(sep='&', index=False, header=False)
    content = content.replace('&', ' & ').replace('\n', r' \\' + '\n')
    return header + content + footer

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("action", choices=['manual', 'plot', 'mean', 'mean-latex'], help="action to be performed")
    parser.add_argument("-s", "--summary", help="csv file with estimation results")
    parser.add_argument("--classified", help="csv file with estimation results")
    parser.add_argument("-o", "--output-file", help="output csv file")
    parser.add_argument("-c", "--chart", choices=['estimators', 'subjects'], help="chart type")
    parser.add_argument("--width", type=float, default=12, help="chart width")
    parser.add_argument("--height", type=float, default=4, help="chart height")
    parser.add_argument("-l", "--label-is-number", action='store_true', help="use number instead of subject name")
    parser.add_argument("-n", "--normalize-mest", action='store_true', help="normalize by Mest (default is normalize by total_mutants)")
    parser.add_argument("-t", "--testsuite", help="which testsuite")
    parser.add_argument("-v", "--valid", action='store_true', help="consider only valid estimations")
    parser.add_argument("--mean-type", choices=['classes', 'methods', 'all'], default='classes', help="mean difference by type")
    args = parser.parse_args()
    action = args.action
    if action == 'manual':
        add_manual_estimation(args.summary, Path(args.classified))
    elif action == 'mean':
        _mean_type = None if args.mean_type == 'all' else args.mean_type
        get_mean_difference(args.summary, args.output_file, args.testsuite, _mean_type, args.valid)
    if action == 'mean-latex':
        get_latex_mean_tables(args.summary, Path(args.output_file), args.valid)

    elif action == 'plot':
        if args.chart == 'estimators':
            ggplot_estimators(args.summary, Path(args.output_file), args.width, args.height, numbered_subject=args.label_is_number,
                              norm_by_mest=args.normalize_mest)
        elif args.chart == 'subjects':
            ggplot_subject_testsuites(args.summary, Path(args.output_file), args.width, args.height, numbered_subject=args.label_is_number)
