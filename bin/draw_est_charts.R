require(data.table)
require(argparse)
require(ggplot2)

make_chart <- function(data, file_name) {
  chart = ggplot(data, aes(x = estimator, y = estimate)) +
    geom_point(size=.5, aes(color=testsuite)) +
    geom_errorbar(aes(ymin = CILower, ymax = CIUpper, color=testsuite), size = 0.4, width = .2) +
    geom_hline(aes(yintercept = total_mutants), linetype = 1, color = 'black') +
    geom_hline(aes(yintercept = Sn, color=testsuite), linetype = 2) +
    coord_flip() +
    expand_limits(y = 0) +
    facet_wrap(~program) +
    theme(axis.text.x = element_text(colour = "black"),
          axis.text.y = element_text(colour = "black"),
          text = element_text(size=8),) + theme(legend.position="none")
  print(paste("Saving", file_name))
  #pdf(file_name)
  ggsave(file_name, chart, height=1.5, width=3)

  #dev.off()
}

draw_charts <- function(input_path, output_dir, testsuite_) {
  print(input_path)
  sdata = fread(input_path)
  #sdata = sdata[testsuite == testsuite_]
  sdata = sdata[estimator != "Manual"]
  subjects = unique(sdata[testsuite == testsuite_, .(program)])[[1]]
  dir.create(paste0(output_dir, '/', testsuite_))
  for (subject_ in subjects) {
    chart_file = paste0(output_dir, '/', testsuite_, '/', subject_, '.pdf')
    subj_data = sdata[program == subject_]
    print(paste("Plotting", subject_))
    make_chart(subj_data, chart_file)
  }
}

if (sys.nframe() == 0 || TRUE) {
  parser = ArgumentParser()
  parser$add_argument("-s", "--summary", help = "summary table")
  parser$add_argument("-t", "--testsuite", help = "testsuite to draw")
  parser$add_argument("-o", "--output", help = "Filename for the resulting comparison table")
  args = parser$parse_args()
  my_data <- args$summary
  draw_charts(my_data, args$output, args$testsuite)
}
